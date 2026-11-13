package me.whereareiam.yui.gradle;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

/**
 * {@code yuiPlugin { }}: what {@code yui-plugin.yml} says of the plugin.
 */
public abstract class YuiPluginExtension {
	/**
	 * @return the plugin's id, such as {@code onboarding}; its folder is {@code plugins/<id>/}
	 */
	public abstract Property<String> getId();

	/**
	 * @return the binary name of its main class, which carries {@code @Plugin}
	 */
	public abstract Property<String> getMain();

	/**
	 * @return the ids of the plugins it needs, which start first
	 */
	public abstract ListProperty<String> getDependencies();

	/**
	 * @return the packages plugins that depend on it see, such as its -api's
	 */
	public abstract ListProperty<String> getExports();

	/**
	 * @return where its releases are found, as {@code <provider>:<id>}, such as {@code GITHUB:whereareiam/Yui}; empty
	 * when it is not checked for updates
	 */
	public abstract Property<String> getUpdates();
}
