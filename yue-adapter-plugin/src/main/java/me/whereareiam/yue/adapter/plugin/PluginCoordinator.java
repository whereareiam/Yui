package me.whereareiam.yue.adapter.plugin;

import me.whereareiam.yue.api.output.plugin.PluginService;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PluginCoordinator implements PluginService {
	private final PluginManager pluginManager;

	@Autowired
	public PluginCoordinator(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

	@Override
	public void loadPlugins() {
		pluginManager.loadPlugins();
		pluginManager.startPlugins();
	}

	@Override
	public void unloadPlugins() {
		pluginManager.stopPlugins();
		pluginManager.unloadPlugins();
	}

	@Override
	public void reloadPlugins() {
		unloadPlugins();
		loadPlugins();
	}
}
