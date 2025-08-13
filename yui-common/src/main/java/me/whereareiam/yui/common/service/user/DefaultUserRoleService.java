package me.whereareiam.yui.common.service.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.input.UserRoleService;
import me.whereareiam.yui.api.model.profile.UserProfile;
import me.whereareiam.yui.api.output.service.RoleService;
import me.whereareiam.yui.api.output.service.UserProfileService;
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
	private final RoleService roleService;
	private final UserProfileService userProfileService;
	private final ExecutorService syncPool;
	private final JDA jda;

	// Simple flag to prevent concurrent syncs for the same user
	private final Set<Long> syncing = new HashSet<>();

	/**
	 * Check if a user is currently being synced by the bot
	 */
	public boolean isUserBeingSynced(long userId) {
		return syncing.contains(userId);
	}

	@Override
	public void addRoleToUser(long userId, long roleId) {
		if (!roleService.roleExists(roleId)) {
			log.warn("Role id={} is not registered in DB – refusing.", roleId);
			return;
		}
		
		// Ensure user profile exists
		Optional<UserProfile> profile = userProfileService.getProfile(userId);
		if (profile.isEmpty()) {
			profile = userProfileService.createProfile(userId);
			if (profile.isEmpty()) {
				log.warn("Failed to create user profile for user {} - cannot add role", userId);
				return;
			}
		}
		
		// Check if user already has this role
		UserProfile userProfile = profile.get();
		long[] currentRoles = userProfile.getRoles();
		if (currentRoles != null) {
			for (long role : currentRoles) {
				if (role == roleId) {
					log.debug("User {} already has role {} - skipping", userId, roleId);
					return;
				}
			}
		}
		
		// Add role to database
		userProfileService.addRole(userId, roleId);
		
		// Sync to Discord
		enqueueSync(userId);
	}

	@Override
	public void removeRoleFromUser(long userId, long roleId) {
		userProfileService.removeRole(userId, roleId);
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
		
		log.info("Starting role sync for all members...");
		guild.loadMembers().onSuccess(members -> {
			members.forEach(member -> syncPool.execute(() -> syncUser(member.getIdLong())));
		}).onError(err -> log.error("Global role sync failed!", err));
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
		Set<Long> allowedRoles = Arrays.stream(roleService.getAvailableRoles())
				.boxed().collect(Collectors.toSet());
		
		// Get current Discord roles (filtered to only allowed ones)
		Set<Long> currentRoles = member.getRoles().stream()
				.map(Role::getIdLong)
				.filter(allowedRoles::contains)
				.collect(Collectors.toSet());
		
		// Get desired roles from database
		Optional<UserProfile> profileOpt = userProfileService.getProfile(userId);
		if (profileOpt.isEmpty()) {
			// No profile found - this might be a new user or DB is slow
			// Don't remove existing roles, just log and wait
			log.debug("No user profile found for user {} - skipping sync to prevent role loss", userId);
			return;
		}
		
		UserProfile profile = profileOpt.get();
		Set<Long> desiredRoles = Arrays.stream(
				Optional.ofNullable(profile.getRoles()).orElse(new long[0])
		).boxed().collect(Collectors.toSet());
		
		// Calculate what needs to change
		Set<Long> toAdd = new HashSet<>(desiredRoles);
		toAdd.removeAll(currentRoles);
		
		Set<Long> toRemove = new HashSet<>(currentRoles);
		toRemove.removeAll(desiredRoles);
		
		// If nothing to change, we're done
		if (toAdd.isEmpty() && toRemove.isEmpty()) {
			log.debug("User {} roles are already in sync", userId);
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
						_ -> log.debug("Successfully synced roles for user {}: +{} -{}", 
								userId, toAdd.size(), toRemove.size()),
						err -> log.error("Role sync failed for user {}", userId, err)
				);
	}

	private void enqueueSync(long userId) {
		syncPool.execute(() -> syncUser(userId));
	}

	private Guild getGuild() {
		return jda.getGuilds().getFirst();
	}

	private Member getMember(Guild guild, long userId) {
		try {
			Member cached = guild.getMemberById(userId);
			return cached != null ? cached : guild.retrieveMemberById(userId).complete();
		} catch (Exception e) {
			log.debug("Failed to fetch member {}: {}", userId, e.getMessage());
			return null;
		}
	}
}