package me.whereareiam.yui.common.service.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.persistence.FluctlightPersistence;
import me.whereareiam.yui.service.UserRoleService;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.persistence.RolePersistence;
import me.whereareiam.yui.fluctlight.FluctlightService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class DefaultUserRoleService implements UserRoleService {
	private final RolePersistence rolePersistence;
	private final FluctlightService fluctlightService;
	private final FluctlightPersistence fluctlightPersistence;
	private final ExecutorService scheduledPool;
	private final JDA jda;

	// Simple flag to prevent concurrent syncs for the same user
	private final Set<Long> syncing = new HashSet<>();

	@Override
	public void addRoleToUser(long userId, long roleId) {
		if (!rolePersistence.roleExists(roleId)) {
			log.warn("[UserRoleService]: Role id={} is not registered in DB – refusing.", roleId);
			return;
		}

		// Ensure Fluctlight exists
		Optional<Fluctlight> fluctlightOpt = fluctlightService.get(userId);
		if (fluctlightOpt.isEmpty()) {
			fluctlightOpt = Optional.of(fluctlightService.getOrCreate(userId));
		}

		// Check if fluctlight already has this allowed role
		Fluctlight fluctlight = fluctlightOpt.get();
		long[] currentRoles = fluctlight.getAllowedRoles();
		if (currentRoles != null) {
			for (long role : currentRoles) {
				if (role == roleId) {
					log.debug("[UserRoleService]: User {} already has allowed role {} - skipping", userId, roleId);
					return;
				}
			}
		}

		// Add allowed role to database
		fluctlightPersistence.addAllowedRole(fluctlight, roleId);
		// Reload to update cache
		fluctlightService.get(userId);

		// Sync to Discord
		enqueueSync(userId);
	}

	@Override
	public void removeRoleFromUser(long userId, long roleId) {
		// Ensure Fluctlight exists
		Optional<Fluctlight> fluctlightOpt = fluctlightService.get(userId);
		if (fluctlightOpt.isEmpty()) {
			fluctlightOpt = Optional.of(fluctlightService.getOrCreate(userId));
		}
		
		Fluctlight fluctlight = fluctlightOpt.get();
		fluctlightPersistence.removeAllowedRole(fluctlight, roleId);
		// Reload to update cache
		fluctlightService.get(userId);
		enqueueSync(userId);
	}

	@Override
	public void syncUser(long userId) {
		// Prevent concurrent syncs for the same user
		if (!syncing.add(userId)) {
			enqueueSync(userId);
			return;
		}

		try {
			syncUserRoles(userId);
		} finally {
			syncing.remove(userId);
		}
	}

	@Override
	public void syncAll() {
		Guild guild = getGuild();
		if (guild == null) return;

		log.debug("[UserRoleService]: Starting role sync for all members...");
		guild.loadMembers().onSuccess(members -> {
			members.forEach(member -> scheduledPool.execute(() -> syncUser(member.getIdLong())));
		}).onError(err -> log.error("[UserRoleService]: Global role sync failed!", err));
	}

	/**
	 * Core sync logic - simple and straightforward
	 */
	private void syncUserRoles(long userId) {
		Guild guild = getGuild();
		if (guild == null) return;

		Member member = getMember(guild, userId);
		if (member == null) return;

		// Get allowed roles from configuration
		Set<Long> allowedRoles = Arrays.stream(rolePersistence.getAvailableRoles())
				.boxed().collect(Collectors.toSet());

		// Get current Discord roles (filtered to only allowed ones)
		Set<Long> currentRoles = member.getRoles().stream()
				.map(Role::getIdLong)
				.filter(allowedRoles::contains)
				.collect(Collectors.toSet());

		// Get desired allowed roles from database
		Optional<Fluctlight> fluctlightOpt = fluctlightService.get(userId);
		if (fluctlightOpt.isEmpty()) {
			// No Fluctlight found - this might be a new fluctlight or DB is slow
			// Don't remove existing roles, just log and wait
			log.debug("[UserRoleService]: No user profile found for user {} - skipping sync to prevent role loss", userId);
			return;
		}

		Fluctlight fluctlight = fluctlightOpt.get();
		Set<Long> desiredRoles = Arrays.stream(
				Optional.ofNullable(fluctlight.getAllowedRoles()).orElse(new long[0])
		).boxed().collect(Collectors.toSet());

		// Calculate what needs to change
		Set<Long> toAdd = new HashSet<>(desiredRoles);
		toAdd.removeAll(currentRoles);

		Set<Long> toRemove = new HashSet<>(currentRoles);
		toRemove.removeAll(desiredRoles);

		// If nothing to change, we're done
		if (toAdd.isEmpty() && toRemove.isEmpty()) {
			log.debug("[UserRoleService]: User {} roles are already in sync", userId);
			return;
		}

		// Apply changes to Discord
		List<Role> addRoles = toAdd.stream()
				.map(guild::getRoleById)
				.filter(Objects::nonNull)
				.toList();

		List<Role> removeRoles = toRemove.stream()
				.map(guild::getRoleById)
				.filter(Objects::nonNull)
				.toList();

		guild.modifyMemberRoles(member, addRoles, removeRoles)
				.reason("Yui automatic role sync")
				.queue(
						_ -> log.debug("[UserRoleService]: Successfully synced roles for user {}: +{} -{}",
								userId, toAdd.size(), toRemove.size()),
						err -> log.error("[UserRoleService]: Role sync failed for user {}", userId, err)
				);
	}

	private void enqueueSync(long userId) {
		scheduledPool.execute(() -> syncUser(userId));
	}

	private Guild getGuild() {
		return jda.getGuilds().getFirst();
	}

	private Member getMember(Guild guild, long userId) {
		try {
			Member cached = guild.getMemberById(userId);
			return cached != null ? cached : guild.retrieveMemberById(userId).complete();
		} catch (Exception e) {
			log.debug("[UserRoleService]: Failed to fetch member {}: {}", userId, e.getMessage());
			return null;
		}
	}
}