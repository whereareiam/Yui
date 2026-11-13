package me.whereareiam.yui.type.update;

/**
 * What the updater posts a warning about, as an audit entry of category {@code system}.
 */
public enum UpdateWarning {
	/**
	 * A newer release is available.
	 */
	UPDATES,

	/**
	 * Yui runs a build made on a developer's machine.
	 */
	LOCAL_BUILDS,

	/**
	 * Yui runs a development build, not a release.
	 */
	DEV_BUILDS
}
