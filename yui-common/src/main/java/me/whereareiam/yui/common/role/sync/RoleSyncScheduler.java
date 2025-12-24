package me.whereareiam.yui.common.role.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.common.config.provider.RolesProvider;
import me.whereareiam.yui.event.fluctlight.FluctlightClearedEvent;
import me.whereareiam.yui.fluctlight.FluctlightRegistry;
import me.whereareiam.yui.model.ManagedRole;
import me.whereareiam.yui.model.config.roles.Roles;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.service.RoleService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Self-contained role synchronization system.
 * <p>
 * This service manages the complete lifecycle of role synchronization:
 * <ul>
 *   <li>Collects pending role changes per user</li>
 *   <li>Debounces rapid changes to prevent rate limiting</li>
 *   <li>Applies changes to Discord in batches</li>
 *   <li>Listens to external Discord role changes and re-syncs (JDA events)</li>
 *   <li>Listens to internal app events that require sync (Spring events)</li>
 * </ul>
 * <p>
 * JDA's internal rate limiter handles all Discord API rate limiting, so this
 * scheduler only focuses on debouncing and batching user-level changes.
 * <p>
 * By extending ListenerAdapter (JDA events) and using @EventListener (Spring events),
 * this service is fully self-contained and handles all role sync concerns in one place.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleSyncScheduler extends ListenerAdapter {
	private final FluctlightRegistry fluctlightRegistry;
	private final ObjectProvider<Settings> settings;
	private final RolesProvider rolesProvider;
	private final RoleService roleService;
	private final JDA jda;

	private final ConcurrentHashMap<Long, PendingRoleSync> pendingChanges = new ConcurrentHashMap<>();

	/**
	 * Handles external role additions from Discord.
	 * <p>
	 * When a role is added externally (e.g., by a server admin), we detect it
	 * and force a re-sync to ensure Discord matches our in-memory state.
	 * This maintains consistency even when changes happen outside our bot.
	 *
	 * @param event The role add event from JDA
	 */
	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
		long userId = event.getUser().getIdLong();

		// External role change detected - force re-sync to maintain consistency
		// Only sync if user is already cached (they'll be synced when they interact otherwise)
		fluctlightRegistry.getFluctlight(userId).ifPresent(fluctlight -> {
			forceSyncNow(fluctlight);
			log.debug("External role add detected for user {}, forcing re-sync", userId);
		});
	}

	/**
	 * Handles external role removals from Discord.
	 * <p>
	 * When a role is removed externally (e.g., by a server admin), we detect it
	 * and force a re-sync to ensure Discord matches our in-memory state.
	 * This maintains consistency even when changes happen outside our bot.
	 *
	 * @param event The role remove event from JDA
	 */
	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
		long userId = event.getUser().getIdLong();

		// External role change detected - force re-sync to maintain consistency
		// Only sync if user is already cached (they'll be synced when they interact otherwise)
		fluctlightRegistry.getFluctlight(userId).ifPresent(fluctlight -> {
			forceSyncNow(fluctlight);
			log.debug("External role remove detected for user {}, forcing re-sync", userId);
		});
	}

	/**
	 * Handles Fluctlight clear events.
	 * <p>
	 * When a Fluctlight is cleared, we need to ensure Discord roles are synced
	 * to match the cleared state. This is a critical operation that:
	 * <ul>
	 *   <li>Cancels any pending changes (they're now obsolete)</li>
	 *   <li>Forces immediate sync (bypasses debounce)</li>
	 * </ul>
	 *
	 * @param event The Fluctlight cleared event from Spring
	 */
	@Order(1)
	@EventListener
	public void onFluctlightCleared(FluctlightClearedEvent event) {
		// Cancel any pending changes - they're obsolete after clear
		cancelPendingChanges(event.getUserId());
		
		// Force immediate sync to remove roles in Discord
		forceSyncNow(event.getNewFluctlight());
		
		log.debug("Force synced roles for user {} after Fluctlight clear", event.getUserId());
	}

	/**
	 * Queues a role change for a user.
	 * This method is non-blocking and returns immediately.
	 *
	 * @param userId The user ID
	 * @param roleId The role ID
	 * @param isAdd  true to add the role, false to remove
	 */
	public void queueRoleChange(long userId, long roleId, boolean isAdd) {
		PendingRoleSync sync = pendingChanges.computeIfAbsent(userId, PendingRoleSync::new);
		
		if (isAdd) {
			sync.addRole(roleId);
			log.trace("Queued role add: userId={}, roleId={}", userId, roleId);
			return;
		}

		sync.removeRole(roleId);
		log.trace("Queued role remove: userId={}, roleId={}", userId, roleId);
	}

	/**
	 * Cancels all pending changes for a user.
	 * Useful when a user's Fluctlight is cleared.
	 *
	 * @param userId The user ID
	 */
	public void cancelPendingChanges(long userId) {
		PendingRoleSync removed = pendingChanges.remove(userId);
		if (removed != null)
			log.debug("Cancelled pending sync for user {}", userId);
	}

	/**
	 * Forces immediate sync for a user, bypassing debounce.
	 * Used for critical operations like Fluctlight clear.
	 *
	 * @param fluctlight The Fluctlight to sync
	 */
	public void forceSyncNow(Fluctlight fluctlight) {
		log.debug("Force syncing user {}", fluctlight.getId());
		
		// Cancel any pending changes first
		cancelPendingChanges(fluctlight.getId());
		
		// Perform immediate sync based on current in-memory state
		syncFluctlightRoles(fluctlight);
	}

	/**
	 * Processes pending changes periodically.
	 * Runs every 100ms to check for users ready to sync after debounce period.
	 */
	@Scheduled(fixedDelay = 100)
	private void processQueue() {
		if (pendingChanges.isEmpty()) return;

		Roles rolesConfig = rolesProvider.get();
		if (rolesConfig == null || rolesConfig.getSettings() == null) return;

		long debounceMillis = rolesConfig.getSettings().getDebounceMillis();

		// Collect users ready to process
		List<PendingRoleSync> readyToProcess = new ArrayList<>();
		for (PendingRoleSync sync : pendingChanges.values()) {
			if (sync.isReadyToProcess(debounceMillis)) {
				sync.markProcessing();
				readyToProcess.add(sync);
			}
		}

		// Process each ready user
		for (PendingRoleSync sync : readyToProcess) {
			try {
				processUserSync(sync);
			} catch (Exception e) {
				log.error("Error processing sync for user {}", sync.getUserId(), e);
			} finally {
				pendingChanges.remove(sync.getUserId());
			}
		}
	}

	/**
	 * Processes synchronization for a single user.
	 *
	 * @param sync The pending sync to process
	 */
	private void processUserSync(PendingRoleSync sync) {
		Guild guild = getGuild();
		if (guild == null) {
			log.warn("Cannot sync roles: guild not found");
			return;
		}

		Member member = guild.getMemberById(sync.getUserId());
		if (member == null) {
			log.debug("Member {} not found in guild, skipping sync", sync.getUserId());
			return;
		}

		// Get the role changes to apply
		Set<Long> rolesToAdd = sync.consumeRolesToAdd();
		Set<Long> rolesToRemove = sync.consumeRolesToRemove();

		// Apply role additions
		for (long roleId : rolesToAdd) {
			if (!isSyncEnabled(roleId)) continue;

			Role role = guild.getRoleById(roleId);
			if (role == null) {
				log.warn("Role {} not found in guild", roleId);
				continue;
			}

			// JDA handles rate limiting internally - just queue the request
			guild.addRoleToMember(member, role).queue(
					_ -> log.debug("Added role {} to user {}", roleId, sync.getUserId()),
					error -> log.error("Failed to add role {} to user {}", roleId, sync.getUserId(), error)
			);
		}

		// Apply role removals
		for (long roleId : rolesToRemove) {
			if (!isSyncEnabled(roleId)) continue;

			Role role = guild.getRoleById(roleId);
			if (role == null) {
				log.warn("Role {} not found in guild", roleId);
				continue;
			}

			// JDA handles rate limiting internally - just queue the request
			guild.removeRoleFromMember(member, role).queue(
					_ -> log.debug("Removed role {} from user {}", roleId, sync.getUserId()),
					error -> log.error("Failed to remove role {} from user {}", roleId, sync.getUserId(), error)
			);
		}

		log.debug("Processed sync for user {}: added={}, removed={}", 
			sync.getUserId(), rolesToAdd.size(), rolesToRemove.size());
	}

	/**
	 * Syncs all roles for a Fluctlight based on current in-memory state.
	 * This compares what the user should have vs what Discord has.
	 *
	 * @param fluctlight The Fluctlight to sync
	 */
	private void syncFluctlightRoles(Fluctlight fluctlight) {
		Guild guild = getGuild();
		if (guild == null) {
			log.warn("Cannot sync roles: guild not found");
			return;
		}

		Member member = guild.getMemberById(fluctlight.getId());
		if (member == null) {
			log.debug("Member {} not found in guild, skipping sync", fluctlight.getId());
			return;
		}

		// Get roles user should have from in-memory state
		long[] allowedRoles = fluctlight.getAllowedRoles();
		Set<Long> shouldHaveRoles = new java.util.HashSet<>();
		if (allowedRoles != null)
			for (long roleId : allowedRoles)
				shouldHaveRoles.add(roleId);

		// Get current Discord roles
		Set<Long> currentDiscordRoles = new java.util.HashSet<>();
		for (Role role : member.getRoles())
			currentDiscordRoles.add(role.getIdLong());

		// Check each sync-enabled role (includes both config and API-added roles)
		List<ManagedRole> allRoles = roleService.getAllRoles();
		if (allRoles == null || allRoles.isEmpty()) return;

		for (ManagedRole managedRole : allRoles) {
			if (managedRole == null || !managedRole.isSync()) continue;

			long roleId = managedRole.getId();
			boolean shouldHave = shouldHaveRoles.contains(roleId);
			boolean hasInDiscord = currentDiscordRoles.contains(roleId);

			if (shouldHave && !hasInDiscord) {
				Role role = guild.getRoleById(roleId);
				if (role != null) {
					guild.addRoleToMember(member, role).queue(
							_ -> log.debug("Added role {} to user {}", roleId, fluctlight.getId()),
						error -> log.error("Failed to add role {} to user {}", roleId, fluctlight.getId(), error)
					);
				}

				continue;
			}

			if (!shouldHave && hasInDiscord) {
				Role role = guild.getRoleById(roleId);
				if (role != null) {
					guild.removeRoleFromMember(member, role).queue(
							_ -> log.debug("Removed role {} from user {}", roleId, fluctlight.getId()),
						error -> log.error("Failed to remove role {} from user {}", roleId, fluctlight.getId(), error)
					);
				}
			}
		}
	}

	/**
	 * Checks if sync is enabled for a role.
	 * This now properly checks both config roles AND API-added roles.
	 *
	 * @param roleId The role ID
	 * @return true if sync is enabled
	 */
	private boolean isSyncEnabled(long roleId) {
		return roleService.isSyncEnabled(roleId);
	}

	/**
	 * Gets the configured guild.
	 *
	 * @return The guild or null if not found
	 */
	private Guild getGuild() {
		return jda.getGuildById(settings.getObject().getDiscord().getGuildId());
	}
}
