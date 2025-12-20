package me.whereareiam.yui.service;

import me.whereareiam.yui.model.config.roles.RoleEntry;
import me.whereareiam.yui.model.fluctlight.Fluctlight;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for managing roles and role synchronization.
 * <p>
 * This service manages roles that the bot is allowed to work with, combining
 * roles defined in configuration files with roles added programmatically via API.
 * It also handles synchronization of user roles between the database/in-memory
 * cache and Discord, ensuring consistency.
 * <p>
 * Roles can be defined in two ways:
 * <ul>
 *   <li>Configuration-based: Defined in roles.yml, persisted across restarts</li>
 *   <li>API-based: Added programmatically via {@link #addAllowedRole(RoleEntry)},
 *       stored in-memory only, does not persist to config</li>
 * </ul>
 * When both exist for the same role ID, API-added roles take precedence.
 */
@SuppressWarnings("unused")
public interface RoleService {
	/**
	 * Checks if a role is allowed (tracked in config or API).
	 * <p>
	 * Determines whether the bot is allowed to work with a role by checking
	 * if it exists in either the configuration or API-added roles.
	 *
	 * @param roleId The role ID to check
	 * @return true if the role is allowed, false otherwise
	 */
	boolean isRoleAllowed(long roleId);

	/**
	 * Checks if sync is enabled for a role.
	 * <p>
	 * Determines whether role synchronization is enabled for a specific role.
	 * Sync-enabled roles are automatically synchronized between the database/in-memory
	 * cache and Discord.
	 *
	 * @param roleId The role ID to check
	 * @return true if sync is enabled for the role, false otherwise
	 */
	boolean isSyncEnabled(long roleId);

	/**
	 * Adds a role via API (stored in-memory only, not persisted to config).
	 * <p>
	 * Adds a role programmatically that the bot is allowed to work with.
	 * This role is stored in-memory only and will be lost on restart.
	 * To persist a role permanently, add it to the roles.yml configuration file.
	 * <p>
	 * If a role with the same ID already exists (either from config or API),
	 * this will overwrite it with the new role entry.
	 *
	 * @param role The role entry to add (must not be null and must have a valid ID)
	 * @throws IllegalArgumentException if the role is null or has an invalid ID
	 */
	void addAllowedRole(RoleEntry role);

	/**
	 * Removes an API-added role (only works for roles added via API, not config roles).
	 * <p>
	 * Removes a role that was previously added via {@link #addAllowedRole(RoleEntry)}.
	 * This method only works for API-added roles; config roles cannot be removed
	 * via this method and must be removed from the roles.yml file.
	 *
	 * @param roleId The role ID to remove
	 * @return true if the role was removed, false if it wasn't found or was a config role
	 */
	boolean removeAllowedRole(long roleId);

	/**
	 * Updates the sync setting for a role (works for both config and API-added roles).
	 * <p>
	 * Updates the synchronization setting for a role. This works for API-added roles
	 * by updating them directly. For config roles, this operation will fail as config
	 * roles cannot be modified via API; they must be updated in the roles.yml file.
	 *
	 * @param roleId The role ID to update
	 * @param sync The new sync setting
	 * @return true if the sync setting was updated, false otherwise (e.g., role not found or is a config role)
	 */
	boolean updateRoleSync(long roleId, boolean sync);

	/**
	 * Syncs a user's roles to Discord (ensures Discord matches DB/in-memory).
	 * <p>
	 * Synchronizes a user's roles between the database/in-memory cache (source of truth)
	 * and Discord. This method:
	 * <ul>
	 *   <li>Adds roles in Discord that the user should have but doesn't have</li>
	 *   <li>Removes roles from Discord that the user shouldn't have but does have</li>
	 * </ul>
	 * Only roles with sync enabled are synchronized.
	 * <p>
	 * This operation is performed asynchronously and returns a CompletableFuture
	 * that completes when the sync operation finishes.
	 *
	 * @param fluctlight The Fluctlight instance representing the user
	 * @return CompletableFuture that completes when the sync operation finishes
	 */
	CompletableFuture<Void> syncUserRoles(Fluctlight fluctlight);

	/**
	 * Syncs all cached users.
	 * <p>
	 * Synchronizes roles for all users that are currently cached in the FluctlightRegistry.
	 * This is useful for bulk synchronization operations. Each user is synced using
	 * {@link #syncUserRoles(Fluctlight)}.
	 * <p>
	 * This operation is performed asynchronously and returns a CompletableFuture
	 * that completes when all sync operations finish.
	 *
	 * @return CompletableFuture that completes when all sync operations finish
	 */
	CompletableFuture<Void> syncAllUsers();

	/**
	 * Gets all roles (config + API-added) merged together.
	 * <p>
	 * Returns a list of all roles that the bot is allowed to work with,
	 * combining roles from the configuration file and roles added via API.
	 * API-added roles take precedence over config roles when there are duplicates.
	 *
	 * @return List of all allowed roles
	 */
	List<RoleEntry> getAllRoles();

	/**
	 * Gets a role by ID (checks both config and API-added roles).
	 * <p>
	 * Searches for a role with the specified ID in both the configuration
	 * and API-added roles. API-added roles are checked first and take precedence.
	 *
	 * @param roleId The role ID to look up
	 * @return Optional containing the role if found, empty otherwise
	 */
	Optional<RoleEntry> getRole(long roleId);
}

