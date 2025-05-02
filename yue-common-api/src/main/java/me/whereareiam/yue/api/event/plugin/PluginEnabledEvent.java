package me.whereareiam.yue.api.event.plugin;

import me.whereareiam.yue.api.event.Cancellable;
import me.whereareiam.yue.api.event.plugin.base.PluginLifecycleEvent;
import me.whereareiam.yue.api.model.plugin.InternalPlugin;

public class PluginEnabledEvent implements PluginLifecycleEvent, Cancellable {
	private final InternalPlugin plugin;
	private boolean cancelled = false;

	public PluginEnabledEvent(InternalPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public InternalPlugin getPlugin() {
		return plugin;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
