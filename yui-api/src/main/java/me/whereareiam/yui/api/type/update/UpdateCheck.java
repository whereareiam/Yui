package me.whereareiam.yui.api.type.update;

/**
 * What the updater looks for new versions of.
 */
public enum UpdateCheck {
	/**
	 * Yui itself.
	 */
	YUI,

	/**
	 * Every loaded plugin that names where its releases are.
	 */
	PLUGINS
}
