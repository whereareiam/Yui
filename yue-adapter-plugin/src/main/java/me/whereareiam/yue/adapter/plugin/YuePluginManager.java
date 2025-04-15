package me.whereareiam.yue.adapter.plugin;

import me.whereareiam.yue.adapter.plugin.descriptor.JsonPluginDescriptorFinder;
import me.whereareiam.yue.api.model.YuePluginDescriptor;
import me.whereareiam.yue.api.output.config.ConfigurationLoader;
import org.pf4j.*;
import org.pf4j.spring.SpringPluginManager;

import java.nio.file.Path;

public class YuePluginManager extends SpringPluginManager {
	private final ConfigurationLoader configurationLoader;

	public YuePluginManager(Path pluginsPath, ConfigurationLoader configurationLoader) {
		this.configurationLoader = configurationLoader;

		super(pluginsPath);
	}

	@Override
	protected PluginDescriptorFinder createPluginDescriptorFinder() {
		return new JsonPluginDescriptorFinder(configurationLoader);
	}

	@Override
	protected PluginRepository createPluginRepository() {
		return new CompoundPluginRepository()
				.add(new JarPluginRepository(getPluginsRoots()), this::isNotDevelopment);
	}

	@Override
	protected PluginLoader createPluginLoader() {
		return new CompoundPluginLoader()
				.add(new JarPluginLoader(this), this::isNotDevelopment);
	}

	@Override
	protected String getPluginLabel(PluginDescriptor pluginDescriptor) {
		if (pluginDescriptor instanceof YuePluginDescriptor descriptor)
			return descriptor.getName() + "@" + descriptor.getVersion();

		return super.getPluginLabel(pluginDescriptor);
	}
}