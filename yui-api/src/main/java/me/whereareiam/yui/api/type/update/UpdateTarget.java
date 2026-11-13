package me.whereareiam.yui.api.type.update;

/**
 * Target type for update checking.
 */
public enum UpdateTarget {
	/**
	 * Check for updates to the Yui core framework.
	 */
	CORE,

	/**
	 * Check for updates to a specific plugin (requires selection).
	 */
	PLUGIN,

	/**
	 * Check for updates to all loaded plugins.
	 */
	ALL
}
