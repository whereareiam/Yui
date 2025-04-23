package me.whereareiam.yue.api.event.plugin;

import me.whereareiam.yue.api.event.YueEvent;

public class PluginUnloadedEvent extends YueEvent {
	private final String pluginId;

	public PluginUnloadedEvent(
			Object source,
			String pluginId
	) {
		super(source);

		this.pluginId = pluginId;
	}

	public String getPluginId() {
		return pluginId;
	}
}
