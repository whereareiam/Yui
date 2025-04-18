package me.whereareiam.yue.adapter.plugin;

import org.pf4j.PluginStatusProvider;

public class YuePluginStatusProvider implements PluginStatusProvider {
	@Override
	public boolean isPluginDisabled(String pluginId) {
		return false;
	}

	@Override
	public void disablePlugin(String pluginId) {

	}

	@Override
	public void enablePlugin(String pluginId) {

	}
}
