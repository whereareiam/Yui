package me.whereareiam.yue.common.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yue.api.event.role.RoleAddedEvent;
import me.whereareiam.yue.api.event.role.RoleRemovedEvent;
import me.whereareiam.yue.api.input.UserRoleService;
import me.whereareiam.yue.api.model.profile.UserProfile;
import me.whereareiam.yue.api.output.service.RoleService;
import me.whereareiam.yue.api.util.Users;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
@Service
@AllArgsConstructor
public class DefaultUserRoleService implements UserRoleService {
	private final RoleService roleService;
	private final ApplicationEventPublisher eventPublisher;
	private final JDA jda;

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

		guild.addRoleToMember(member, discordRole)
				.reason("Yue automatic role assign")
				.queue(
						v -> {
							updateProfile(userId, roleId, true);
							eventPublisher.publishEvent(new RoleAddedEvent(userId, roleId));
							log.debug("Role id={} added to user id={}", roleId, userId);
						},
						err -> log.error("Failed adding role id={} to user id={}", roleId, userId, err)
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

		guild.removeRoleFromMember(member, discordRole)
				.reason("Yue automatic role remove")
				.queue(
						v -> {
							updateProfile(userId, roleId, false);
							eventPublisher.publishEvent(new RoleRemovedEvent(userId, roleId));
							log.debug("Role id={} removed from user id={}", roleId, userId);
						},
						err -> log.error("Failed removing role id={} from user id={}", roleId, userId, err)
				);
	}

	@Override
	public void syncUser(long userId) {
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
	}

	@Override
	public void syncAll() {
		Guild guild = resolveGuild();
		guild.loadMembers()
				.onSuccess(members -> members.forEach(member -> syncUser(member.getIdLong())))
				.onError(err -> log.error("Global role-sync failed!", err));
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

	private void updateProfile(long userId, long roleId, boolean add) {
		Users.get(userId).ifPresent(profile -> {
			Set<Long> set = profile.getRoles() == null
					? new HashSet<>()
					: Arrays.stream(profile.getRoles()).boxed().collect(Collectors.toSet());

			if (add) {
				set.add(roleId);
			} else {
				set.remove(roleId);
			}
			profile.setRoles(set.stream().mapToLong(Long::longValue).toArray());
		});
	}
}
