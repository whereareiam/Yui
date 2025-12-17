package me.whereareiam.yui.plugin;

import me.whereareiam.yui.model.plugin.InternalPlugin;
import me.whereareiam.yui.model.plugin.Plugin;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public interface PluginManager {
	void load(Path jar);

	void load(String jarName);

	/**
	 * Reloads the entire plugin system.
	 * <p>
	 * Implementations should unload all currently loaded plugins, then re-initialize
	 * by scanning the plugins directory and loading plugins again in correct order.
	 */
	void reload();

	<T> void injectBean(String beanName, Class<T> beanClass, Supplier<T> supplier);

	void removeInjectedBean(String beanName);

	Optional<YuiPlugin> enable(String id);

	Optional<YuiPlugin> disable(String id);

	Optional<YuiPlugin> unload(String id);

	Optional<InternalPlugin> whichPlugin(Class<?> type);

	Collection<InternalPlugin> plugins();

	/**
	 * Discover loadable plugins from the plugins directory.
	 * Returns a map: jar base name (without .jar) -> parsed plugin descriptor.
	 */
	Map<String, Plugin> loadable();
}