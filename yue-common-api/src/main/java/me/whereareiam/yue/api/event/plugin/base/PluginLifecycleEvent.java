package me.whereareiam.yue.api.event.plugin.base;

import me.whereareiam.yue.api.model.plugin.InternalPlugin;

public interface PluginLifecycleEvent {
	InternalPlugin getPlugin();
}
