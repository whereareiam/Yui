package me.whereareiam.yue.common.service.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yue.api.event.role.RoleAddedEvent;
import me.whereareiam.yue.api.event.role.RoleRemovedEvent;
import me.whereareiam.yue.api.input.UserRoleService;
import me.whereareiam.yue.api.model.profile.UserProfile;
import me.whereareiam.yue.api.output.service.RoleService;
import me.whereareiam.yue.api.output.service.UserProfileService;
import me.whereareiam.yue.api.util.Users;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
@Service
@AllArgsConstructor
public class DefaultUserRoleService implements UserRoleService {
	private final RoleService roleService;
	private final UserProfileService userProfileService;
	private final ApplicationEventPublisher eventPublisher;
	private final JDA jda;

	private static final int PARALLELISM =
			Math.max(2, Runtime.getRuntime().availableProcessors());

	private final ExecutorService syncPool =
			Executors.newFixedThreadPool(PARALLELISM, r -> new Thread(r, "yue-role-sync"));

	private final Set<Long> syncing = ConcurrentHashMap.newKeySet();

	@Override
	public void addRoleToUser(long userId, long roleId) {
		if (!syncing.add(userId))
			return;

		try {
			Guild guild = resolveGuild();
			if (!roleService.roleExists(roleId)) {
				log.warn("Role id={} is not registered in database, refusing to add.", roleId);
				return;
			}

			Role discordRole = guild.getRoleById(roleId);
			if (discordRole == null) {
				log.warn("Role id={} does not exist on the guild.", roleId);
				return;
			}

			Member member = fetchMember(guild, userId);
			if (member == null || member.getRoles().contains(discordRole))
				return;

			guild.addRoleToMember(member, discordRole)
					.reason("Yue automatic role assign")
					.queue(
							v -> {
								updateProfile(userId, Set.of(roleId), Set.of());
								eventPublisher.publishEvent(new RoleAddedEvent(userId, roleId));
								log.debug("Role id={} added to user id={}", roleId, userId);
							},
							err -> log.error("Failed adding role id={} to user id={}", roleId, userId, err)
					);
		} finally {
			syncing.remove(userId);
		}
	}

	@Override
	public void removeRoleFromUser(long userId, long roleId) {
		if (!syncing.add(userId))
			return;

		try {
			Guild guild = resolveGuild();
			Role discordRole = guild.getRoleById(roleId);
			if (discordRole == null) return;

			Member member = fetchMember(guild, userId);
			if (member == null || !member.getRoles().contains(discordRole))
				return;

			guild.removeRoleFromMember(member, discordRole)
					.reason("Yue automatic role remove")
					.queue(
							v -> {
								updateProfile(userId, Set.of(), Set.of(roleId));
								eventPublisher.publishEvent(new RoleRemovedEvent(userId, roleId));
								log.debug("Role id={} removed from user id={}", roleId, userId);
							},
							err -> log.error("Failed removing role id={} from user id={}", roleId, userId, err)
					);
		} finally {
			syncing.remove(userId);
		}
	}

	@Override
	public void syncUser(long userId) {
		if (!syncing.add(userId))
			return;

		try {
			Guild guild = resolveGuild();
			Member member = fetchMember(guild, userId);
			if (member == null) return;

			Set<Long> discordRoles = member.getRoles().stream()
					.map(Role::getIdLong)
					.collect(Collectors.toSet());

			Set<Long> allowedRoles = LongStream.of(roleService.getAvailableRoles())
					.boxed()
					.collect(Collectors.toSet());
			discordRoles.retainAll(allowedRoles);

			UserProfile profile = Users.get(userId).orElse(new UserProfile(userId));
			Set<Long> storedRoles = profile.getRoles() == null
					? new HashSet<>()
					: Arrays.stream(profile.getRoles()).boxed().collect(Collectors.toSet());

			Set<Long> toAdd = new HashSet<>(storedRoles);
			toAdd.removeAll(discordRoles);
			Set<Long> toRemove = new HashSet<>(discordRoles);
			toRemove.removeAll(storedRoles);

			toAdd.forEach(r -> addRoleToUser(userId, r));
			toRemove.forEach(r -> removeRoleFromUser(userId, r));
		} finally {
			syncing.remove(userId);
		}
	}

	@Override
	public void syncAll() {
		Guild guild = resolveGuild();
		Set<Long> allowedRoles = LongStream.of(roleService.getAvailableRoles())
				.boxed()
				.collect(Collectors.toSet());

		guild.loadMembers().onSuccess(members -> {
			log.info("Starting role synchronisation for {} member(s)…", members.size());

			members.forEach(member ->
					syncPool.execute(() -> fastSyncMember(member, allowedRoles))
			);
		}).onError(err -> log.error("Global role-sync failed!", err));
	}

	private Guild resolveGuild() {
		return jda.getGuilds().getFirst();
	}

	private Member fetchMember(Guild guild, long userId) {
		try {
			return guild.retrieveMemberById(userId).complete();
		} catch (Exception ignored) {
			return null;
		}
	}

	/**
	 * Updates the cached {@link UserProfile} instance _and_ persists the change
	 * through {@link UserProfileService}.
	 */
	private void updateProfile(long userId, Set<Long> add, Set<Long> remove) {
		Users.get(userId).ifPresent(p -> {
			Set<Long> roles = p.getRoles() == null
					? new HashSet<>()
					: Arrays.stream(p.getRoles()).boxed().collect(Collectors.toSet());

			roles.addAll(add);
			roles.removeAll(remove);

			p.setRoles(roles.stream().mapToLong(Long::longValue).toArray());
		});

		add.forEach(roleId -> userProfileService.addRole(userId, roleId));
		remove.forEach(roleId -> userProfileService.removeRole(userId, roleId));
	}

	private void fastSyncMember(Member member, Set<Long> allowedRoles) {
		long userId = member.getIdLong();

		Set<Long> onDiscord = member.getRoles().stream()
				.map(Role::getIdLong)
				.filter(allowedRoles::contains)
				.collect(Collectors.toSet());

		Set<Long> stored = Users.get(userId)
				.map(UserProfile::getRoles)
				.map(arr -> Arrays.stream(arr).boxed().collect(Collectors.toSet()))
				.orElseGet(HashSet::new);

		Set<Long> toAdd = new HashSet<>(stored);
		toAdd.removeAll(onDiscord);

		Set<Long> toRemove = new HashSet<>(onDiscord);
		toRemove.removeAll(stored);

		if (toAdd.isEmpty() && toRemove.isEmpty())
			return;

		List<Role> addRoles = toAdd.stream()
				.map(member.getGuild()::getRoleById)
				.filter(Objects::nonNull)
				.toList();

		List<Role> removeRoles = toRemove.stream()
				.map(member.getGuild()::getRoleById)
				.filter(Objects::nonNull)
				.toList();

		// Single REST request for both directions
		member.getGuild()
				.modifyMemberRoles(member, addRoles, removeRoles)
				.reason("Yue automatic role sync")
				.queue(
						_ -> {
							updateProfile(userId, toAdd, toRemove);
							log.debug("Synced roles for user {}", userId);
						},
						err -> log.error("Failed syncing roles for user {}", userId, err)
				);
	}
}