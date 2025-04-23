package me.whereareiam.yue.api.event.plugin;

import me.whereareiam.yue.api.event.YueEvent;

import java.nio.file.Path;

public class PluginLoadedEvent extends YueEvent {
	private final Path pluginPath;

	public PluginLoadedEvent(
			Object source,
			Path pluginPath
	) {
		super(source);

		this.pluginPath = pluginPath;
	}

	public Path getPluginPath() {
		return pluginPath;
	}
}
