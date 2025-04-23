package me.whereareiam.yue.adapter.plugin;

import me.whereareiam.yue.adapter.plugin.descriptor.JsonPluginDescriptorFinder;
import me.whereareiam.yue.api.event.plugin.PluginDisableEvent;
import me.whereareiam.yue.api.event.plugin.PluginEnableEvent;
import me.whereareiam.yue.api.event.plugin.PluginLoadedEvent;
import me.whereareiam.yue.api.event.plugin.PluginUnloadedEvent;
import me.whereareiam.yue.api.model.YuePluginDescriptor;
import me.whereareiam.yue.api.output.config.ConfigurationLoader;
import org.pf4j.*;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class YuePluginManager extends SpringPluginManager {
	private final ApplicationEventPublisher eventPublisher;
	private final ConfigurationLoader configurationLoader;

	public YuePluginManager(
			@Qualifier("pluginsPath") Path pluginsPath,
			ApplicationEventPublisher eventPublisher,
			ConfigurationLoader configurationLoader
	) {
		this.eventPublisher = eventPublisher;
		this.configurationLoader = configurationLoader;

		super(pluginsPath);
	}

	@Override
	public String loadPlugin(Path pluginPath) {
		String context = super.loadPlugin(pluginPath);

		eventPublisher.publishEvent(new PluginLoadedEvent(this, pluginPath));

		return context;
	}

	@Override
	protected PluginWrapper loadPluginFromPath(Path pluginPath) {
		PluginWrapper wrapper = super.loadPluginFromPath(pluginPath);

		eventPublisher.publishEvent(new PluginLoadedEvent(this, pluginPath));

		return wrapper;
	}

	@Override
	public boolean enablePlugin(String pluginId) {
		boolean status = super.enablePlugin(pluginId);

		eventPublisher.publishEvent(new PluginEnableEvent(this, pluginId));

		return status;
	}

	@Override
	public boolean disablePlugin(String pluginId) {
		boolean status = super.disablePlugin(pluginId);

		eventPublisher.publishEvent(new PluginDisableEvent(this, pluginId));

		return status;
	}

	@Override
	public boolean unloadPlugin(String pluginId) {
		boolean status = super.unloadPlugin(pluginId);

		eventPublisher.publishEvent(new PluginUnloadedEvent(this, pluginId));

		return status;
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
	protected PluginStatusProvider createPluginStatusProvider() {
		return new YuePluginStatusProvider();
	}

	@Override
	protected String getPluginLabel(PluginDescriptor pluginDescriptor) {
		if (pluginDescriptor instanceof YuePluginDescriptor descriptor)
			return descriptor.getName() + "@" + descriptor.getVersion();

		return super.getPluginLabel(pluginDescriptor);
	}
}