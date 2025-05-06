package me.whereareiam.yui.common.service.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.input.UserRoleService;
import me.whereareiam.yui.api.model.profile.UserProfile;
import me.whereareiam.yui.api.output.service.RoleService;
import me.whereareiam.yui.api.output.service.UserProfileService;
import me.whereareiam.yui.api.util.Users;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class DefaultUserRoleService implements UserRoleService {
	private final RoleService roleService;
	private final UserProfileService userProfileService;
	private final ApplicationEventPublisher eventPublisher;
	private final ExecutorService syncPool;
	private final JDA jda;

	/**
	 * guards that at most **one** sync task per user runs concurrently
	 */
	private final Set<Long> syncing = ConcurrentHashMap.newKeySet();

	@Override
	public void addRoleToUser(long userId, long roleId) {
		if (!roleService.roleExists(roleId)) {
			log.warn("Role id={} is not registered in DB – refusing.", roleId);
			return;
		}
		updateProfile(userId, Set.of(roleId), Set.of());
		enqueueSync(userId);
	}

	@Override
	public void removeRoleFromUser(long userId, long roleId) {
		updateProfile(userId, Set.of(), Set.of(roleId));
		enqueueSync(userId);
	}

	@Override
	public void syncUser(long userId) {
		if (!syncing.add(userId)) {
			enqueueSync(userId);
			return;
		}
		try {
			Guild guild = resolveGuild();
			Member member = fetchMember(guild, userId);
			if (member == null) return;

			Set<Long> allowed = Arrays.stream(roleService.getAvailableRoles())
					.boxed().collect(Collectors.toSet());

			Set<Long> current = member.getRoles().stream()
					.map(Role::getIdLong)
					.filter(allowed::contains)
					.collect(Collectors.toSet());

			UserProfile p = Users.get(userId).orElse(new UserProfile(userId));
			Set<Long> desired = Arrays.stream(
					Optional.ofNullable(p.getRoles()).orElse(new long[0])
			).boxed().collect(Collectors.toSet());

			Set<Long> toAdd = new HashSet<>(desired);
			toAdd.removeAll(current);
			Set<Long> toRemove = new HashSet<>(current);
			toRemove.removeAll(desired);

			if (toAdd.isEmpty() && toRemove.isEmpty()) return;

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
							_ -> {
								updateProfile(userId, toAdd, toRemove);
								log.debug("Synced roles for user {}", userId);
							},
							err -> log.error("Role sync failed for {}", userId, err)
					);

		} finally {
			syncing.remove(userId);
		}
	}

	@Override
	public void syncAll() {
		Guild guild = resolveGuild();
		Set<Long> allowed = Arrays.stream(roleService.getAvailableRoles())
				.boxed().collect(Collectors.toSet());
		guild.loadMembers().onSuccess(members -> {
			log.info("Starting role‑sync for {} member(s)…", members.size());
			members.forEach(m -> syncPool.execute(() -> fastSyncMember(m, allowed)));
		}).onError(err -> log.error("Global role‑sync failed!", err));
	}

	private void enqueueSync(long userId) {
		syncPool.execute(() -> syncUser(userId));
	}

	private Guild resolveGuild() {
		return jda.getGuilds().getFirst();
	}

	private Member fetchMember(Guild guild, long userId) {
		try {
			Member cached = guild.getMemberById(userId);
			return cached != null ? cached
					: guild.retrieveMemberById(userId).complete();
		} catch (Exception ignored) {
			return null;
		}
	}

	/**
	 * Updates both cache and persistent storage
	 */
	private void updateProfile(long userId, Set<Long> add, Set<Long> rem) {
		Users.get(userId).ifPresent(p -> {
			Set<Long> roles = p.getRoles() == null
					? new HashSet<>()
					: Arrays.stream(p.getRoles()).boxed().collect(Collectors.toSet());
			roles.addAll(add);
			roles.removeAll(rem);
			p.setRoles(roles.stream().mapToLong(Long::longValue).toArray());
		});
		add.forEach(r -> userProfileService.addRole(userId, r));
		rem.forEach(r -> userProfileService.removeRole(userId, r));
	}

	private void fastSyncMember(Member member, Set<Long> allowed) {
		long userId = member.getIdLong();
		Set<Long> current = member.getRoles().stream()
				.map(Role::getIdLong)
				.filter(allowed::contains)
				.collect(Collectors.toSet());
		Optional<UserProfile> cached = Users.get(userId);
		if (cached.isEmpty()) {
			if (!current.isEmpty()) updateProfile(userId, current, Set.of());
			return;
		}
		Set<Long> stored = Arrays.stream(
				Optional.ofNullable(cached.get().getRoles()).orElse(new long[0])
		).boxed().collect(Collectors.toSet());
		Set<Long> toAdd = new HashSet<>(stored);
		toAdd.removeAll(current);
		Set<Long> toRemove = new HashSet<>(current);
		toRemove.removeAll(stored);
		if (toAdd.isEmpty() && toRemove.isEmpty()) return;
		List<Role> addRoles = toAdd.stream().map(member.getGuild()::getRoleById)
				.filter(Objects::nonNull).toList();
		List<Role> removeRoles = toRemove.stream().map(member.getGuild()::getRoleById)
				.filter(Objects::nonNull).toList();
		member.getGuild().modifyMemberRoles(member, addRoles, removeRoles)
				.reason("Yui automatic role boot‑sync")
				.queue(_ -> updateProfile(userId, toAdd, toRemove),
						err -> log.error("Failed syncing roles for user {}", userId, err));
	}
}