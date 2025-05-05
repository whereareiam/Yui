package me.whereareiam.yui.common.service.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.event.role.RoleAddedEvent;
import me.whereareiam.yui.api.event.role.RoleRemovedEvent;
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
			Executors.newFixedThreadPool(PARALLELISM, r -> new Thread(r, "yui-role-sync"));

	private final Set<Long> syncing = ConcurrentHashMap.newKeySet();

	@Override
	public void addRoleToUser(long userId, long roleId) {
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

		updateProfile(userId, Set.of(roleId), Set.of());

		guild.addRoleToMember(member, discordRole)
				.reason("Yui automatic role assign")
				.queue(
						_ -> eventPublisher.publishEvent(new RoleAddedEvent(userId, roleId)),
						err -> {
							log.error("Failed adding role id={} to user id={}", roleId, userId, err);
							updateProfile(userId, Set.of(), Set.of(roleId));
						}
				);
	}

	@Override
	public void removeRoleFromUser(long userId, long roleId) {
		Guild guild = resolveGuild();
		Role discordRole = guild.getRoleById(roleId);
		if (discordRole == null) return;

		Member member = fetchMember(guild, userId);
		if (member == null || !member.getRoles().contains(discordRole))
			return;

		updateProfile(userId, Set.of(), Set.of(roleId));

		guild.removeRoleFromMember(member, discordRole)
				.reason("Yui automatic role remove")
				.queue(
						_ -> eventPublisher.publishEvent(new RoleRemovedEvent(userId, roleId)),
						err -> {
							log.error("Failed removing role id={} from user id={}", roleId, userId, err);
							updateProfile(userId, Set.of(roleId), Set.of());
						}
				);
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

		Optional<UserProfile> cachedProfile = Users.get(userId);
		if (cachedProfile.isEmpty()) {
			if (!onDiscord.isEmpty()) {
				updateProfile(userId, onDiscord, Set.of());
				log.debug("Boot-strap – stored roles of user {} from Discord {}", userId, onDiscord);
			}
			return;
		}

		Set<Long> stored = Arrays.stream(
				Optional.ofNullable(cachedProfile.get().getRoles()).orElse(new long[0])
		).boxed().collect(Collectors.toSet());

		Set<Long> toAdd = new HashSet<>(stored);
		toAdd.removeAll(onDiscord);
		Set<Long> toRemove = new HashSet<>(onDiscord);
		toRemove.removeAll(stored);

		if (toAdd.isEmpty() && toRemove.isEmpty()) return;

		List<Role> addRoles = toAdd.stream()
				.map(member.getGuild()::getRoleById)
				.filter(Objects::nonNull)
				.toList();

		List<Role> removeRoles = toRemove.stream()
				.map(member.getGuild()::getRoleById)
				.filter(Objects::nonNull)
				.toList();

		member.getGuild()
				.modifyMemberRoles(member, addRoles, removeRoles)
				.reason("Yui automatic role sync")
				.queue(
						_ -> {
							updateProfile(userId, toAdd, toRemove);
							log.debug("Synced roles for user {}", userId);
						},
						err -> log.error("Failed syncing roles for user {}", userId, err)
				);
	}
}