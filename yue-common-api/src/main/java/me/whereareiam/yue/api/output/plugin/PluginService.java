package me.whereareiam.yue.api.output.plugin;

/**
 * Service interface for managing plugins in the Yue Discord Bot framework.
 * Provides methods for loading, unloading, and retrieving plugin information.
 *
 * <p>This service handles the lifecycle of plugins and provides
 * access to plugin instances at runtime.</p>
 */
public interface PluginService {
	/**
	 * Loads all available plugins into the plugin system
	 */
	void loadPlugins();

	/**
	 * Unloads all currently active plugins from the system
	 */
	void unloadPlugins();

	/**
	 * Reloads all plugins by unloading and loading them again
	 */
	void reloadPlugins();
}