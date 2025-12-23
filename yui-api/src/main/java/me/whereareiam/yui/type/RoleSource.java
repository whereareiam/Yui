package me.whereareiam.yui.type;

/**
 * Represents the source/origin of a role in the system.
 * <p>
 * Roles can come from two sources:
 * <ul>
 *   <li><b>CONFIG</b>: Defined in roles.yml, persisted across restarts</li>
 *   <li><b>API</b>: Added programmatically via API, temporary (lost on restart)</li>
 * </ul>
 */
public enum RoleSource {
	/**
	 * Role defined in roles.yml configuration file.
	 * These roles are persistent and survive bot restarts.
	 */
	CONFIG,

	/**
	 * Role added programmatically via API at runtime.
	 * These roles are temporary and will be lost when the bot restarts.
	 */
	API
}
