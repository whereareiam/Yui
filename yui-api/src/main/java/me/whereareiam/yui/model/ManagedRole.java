package me.whereareiam.yui.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.model.config.roles.RoleEntry;
import me.whereareiam.yui.type.RoleSource;

/**
 * A managed wrapper around {@link RoleEntry} that provides additional metadata
 * about the role's source and persistence characteristics.
 * <p>
 * This class wraps a {@link RoleEntry} and adds information about where the role
 * came from (config file vs API) and whether it should be persisted to the database.
 * This makes it easier to work with roles without needing to check separate maps
 * or maintain implicit knowledge about role sources.
 */
@Getter
@RequiredArgsConstructor
public class ManagedRole {
	/**
	 * The underlying role entry containing the role's configuration.
	 */
	private final RoleEntry entry;

	/**
	 * The source/origin of this role (CONFIG or API).
	 */
	private final RoleSource source;

	/**
	 * Gets the role's Discord ID.
	 *
	 * @return The role ID
	 */
	public long getId() {
		return entry.getId();
	}

	/**
	 * Gets the role's display name.
	 *
	 * @return The role name, or null if not set
	 */
	public String getName() {
		return entry.getName();
	}

	/**
	 * Gets the role's description.
	 *
	 * @return The role description, or null if not set
	 */
	public String getDescription() {
		return entry.getDescription();
	}

	/**
	 * Checks if synchronization is enabled for this role.
	 * Sync-enabled roles are automatically synchronized between the database
	 * and Discord.
	 *
	 * @return true if sync is enabled, false otherwise
	 */
	public boolean isSync() {
		return entry.isSync();
	}

	/**
	 * Checks if this role is temporary (added via API).
	 * Temporary roles are not persisted across bot restarts.
	 *
	 * @return true if the role is from API (temporary), false if from config
	 */
	public boolean isTemporary() {
		return source == RoleSource.API;
	}

	/**
	 * Checks if this role should be persisted to the database.
	 * Only config roles are persisted; API roles are runtime-only.
	 *
	 * @return true if the role should be persisted, false otherwise
	 */
	public boolean isPersistable() {
		return source == RoleSource.CONFIG;
	}

	@Override
	public String toString() {
		return String.format("ManagedRole{id=%d, name='%s', source=%s, sync=%b}",
				getId(), getName(), source, isSync());
	}
}
