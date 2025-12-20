package me.whereareiam.yui.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.common.config.provider.RolesProvider;
import me.whereareiam.yui.fluctlight.FluctlightRegistry;
import me.whereareiam.yui.model.config.roles.RoleEntry;
import me.whereareiam.yui.model.config.roles.Roles;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.service.RoleService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultRoleService implements RoleService {
	private final RolesProvider rolesProvider;
	private final FluctlightRegistry fluctlightRegistry;
	private final ObjectProvider<Settings> settings;
	private final JDA jda;

	// API-added roles (in-memory only, not persisted to config)
	private final Map<Long, RoleEntry> temporaryRoles = new ConcurrentHashMap<>();

	@Override
	public boolean isRoleAllowed(long roleId) {
		return getRole(roleId).isPresent();
	}

	@Override
	public boolean isSyncEnabled(long roleId) {
		return getRole(roleId)
				.map(RoleEntry::isSync)
				.orElse(false);
	}

	@Override
	public void addAllowedRole(RoleEntry role) {
		if (role == null || role.getId() == 0) {
			throw new IllegalArgumentException("Role entry must not be null and must have a valid ID");
		}
		
		temporaryRoles.put(role.getId(), role);
		log.info("Added role via API: {} (sync: {})", role.getName() != null ? role.getName() : role.getId(), role.isSync());
	}

	@Override
	public boolean removeAllowedRole(long roleId) {
		RoleEntry removed = temporaryRoles.remove(roleId);
		if (removed != null) {
			log.info("Removed API-added role: {}", removed.getName() != null ? removed.getName() : roleId);
			return true;
		}
		return false;
	}

	@Override
	public boolean updateRoleSync(long roleId, boolean sync) {
		// Try API-added roles first
		RoleEntry apiRole = temporaryRoles.get(roleId);
		if (apiRole != null) {
			apiRole.setSync(sync);
			log.info("Updated sync for API-added role {}: {}", apiRole.getName() != null ? apiRole.getName() : roleId, sync);
			return true;
		}
		
		// Try config roles (note: we can't modify config in memory, but we could update API-added override)
		// For now, we'll just log that config roles can't be modified via API
		Roles config = rolesProvider.get();
		if (config != null && config.getRoles() != null) {
			Optional<RoleEntry> configRole = config.getRoles().stream()
					.filter(r -> r.getId() == roleId)
					.findFirst();

			if (configRole.isPresent()) {
				log.warn("Cannot update sync for config role {} via API. Update roles.yml instead.", roleId);
				return false;
			}
		}
		
		return false;
	}

	@Override
	public CompletableFuture<Void> syncUserRoles(Fluctlight fluctlight) {
		return CompletableFuture.runAsync(() -> {
			try {
				Guild guild = getGuild();
				if (guild == null) {
					log.warn("Cannot sync roles: guild not found");
					return;
				}

				Member member = guild.getMemberById(fluctlight.getId());
				if (member == null) {
					log.debug("Member {} not found in guild, skipping role sync", fluctlight.getId());
					return;
				}

				// Get roles user should have from DB/in-memory
				long[] allowedRoles = fluctlight.getAllowedRoles();
				Set<Long> shouldHaveRoles = allowedRoles != null 
						? Arrays.stream(allowedRoles).boxed().collect(Collectors.toSet())
						: Collections.emptySet();

				// Get current Discord roles
				Set<Long> currentDiscordRoles = member.getRoles().stream()
						.map(Role::getIdLong)
						.collect(Collectors.toSet());

				// For each tracked role with sync enabled, ensure Discord matches
				for (RoleEntry roleEntry : getAllRoles()) {
					if (!roleEntry.isSync())
						continue;
					
					long roleId = roleEntry.getId();
					boolean shouldHave = shouldHaveRoles.contains(roleId);
					boolean hasInDiscord = currentDiscordRoles.contains(roleId);

					if (shouldHave && !hasInDiscord) {
						addRoleAsync(guild, member, roleId);
						continue;
					}

					if (!shouldHave && hasInDiscord)
						removeRoleAsync(guild, member, roleId);
				}

				log.debug("Synced roles for user {}", fluctlight.getId());
			} catch (Exception e) {
				log.error("Error syncing roles for user {}", fluctlight.getId(), e);
			}
		});
	}

	@Override
	public CompletableFuture<Void> syncAllUsers() {
		return CompletableFuture.runAsync(() -> {
			Guild guild = getGuild();
			if (guild == null) {
				log.warn("Cannot sync all users: guild not found");
				return;
			}

			log.info("Syncing roles for all guild members");
			guild.loadMembers().onSuccess(members -> {
				int synced = 0;
				for (Member member : members) {
					Optional<Fluctlight> fluctlightOpt = fluctlightRegistry.getFluctlight(member.getIdLong());
					if (fluctlightOpt.isPresent()) {
						syncUserRoles(fluctlightOpt.get());
						synced++;
					}
				}
				log.info("Finished syncing roles for {} cached users", synced);
			}).onError(error -> log.error("Error loading guild members for role sync", error));
		});
	}

	@Override
	public List<RoleEntry> getAllRoles() {
		List<RoleEntry> allRoles = new ArrayList<>();

		Roles config = rolesProvider.get();
		if (config != null && config.getRoles() != null)
			allRoles.addAll(config.getRoles());

		allRoles.addAll(temporaryRoles.values());

		// Remove duplicates (API roles take precedence)
		Map<Long, RoleEntry> unique = new LinkedHashMap<>();
		for (RoleEntry role : allRoles)
			unique.put(role.getId(), role);

		return new ArrayList<>(unique.values());
	}

	@Override
	public Optional<RoleEntry> getRole(long roleId) {
		// Check API-added roles first (they take precedence)
		RoleEntry apiRole = temporaryRoles.get(roleId);
		if (apiRole != null)
			return Optional.of(apiRole);

		// Check config roles
		Roles config = rolesProvider.get();
		return config.getRoles().stream()
				.filter(r -> r.getId() == roleId)
				.findFirst();
	}

	private void addRoleAsync(Guild guild, Member member, long roleId) {
		try {
			Role role = guild.getRoleById(roleId);
			if (role == null) {
				log.warn("Role {} not found in guild", roleId);
				return;
			}

			guild.addRoleToMember(member, role).queue(
					_ -> log.debug("Added role {} to user {}", roleId, member.getId()),
					error -> log.error("Failed to add role {} to user {}", roleId, member.getId(), error)
			);
		} catch (Exception e) {
			log.error("Error adding role {} to user {}", roleId, member.getId(), e);
		}
	}

	private void removeRoleAsync(Guild guild, Member member, long roleId) {
		try {
			Role role = guild.getRoleById(roleId);
			if (role == null) {
				log.warn("Role {} not found in guild", roleId);
				return;
			}

			guild.removeRoleFromMember(member, role).queue(
					_ -> log.debug("Removed role {} from user {}", roleId, member.getId()),
					error -> log.error("Failed to remove role {} from user {}", roleId, member.getId(), error)
			);
		} catch (Exception e) {
			log.error("Error removing role {} from user {}", roleId, member.getId(), e);
		}
	}

	private Guild getGuild() {
		return jda.getGuildById(settings.getObject().getDiscord().getGuildId());
	}
}

