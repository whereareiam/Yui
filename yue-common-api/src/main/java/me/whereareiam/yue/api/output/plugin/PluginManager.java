package me.whereareiam.yue.api.output.plugin;

import me.whereareiam.yue.api.model.plugin.InternalPlugin;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

public interface PluginManager {
	void initialize();

	void load(Path jar);

	<T> void injectBean(String beanName, Class<T> beanClass, Supplier<T> supplier);

	void removeInjectedBean(String beanName);

	Optional<YuePlugin> enable(String id);

	Optional<YuePlugin> disable(String id);

	Optional<YuePlugin> unload(String id);

	Optional<InternalPlugin> whichPlugin(Class<?> type);

	Collection<?> plugins();
}