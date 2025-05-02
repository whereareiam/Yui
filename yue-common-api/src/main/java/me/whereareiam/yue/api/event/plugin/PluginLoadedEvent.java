package me.whereareiam.yue.api.event.plugin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.whereareiam.yue.api.event.Cancellable;
import me.whereareiam.yue.api.event.plugin.base.PluginLifecycleEvent;
import me.whereareiam.yue.api.model.plugin.InternalPlugin;

@Getter
@Setter
@RequiredArgsConstructor
public class PluginLoadedEvent implements PluginLifecycleEvent, Cancellable {
	private final InternalPlugin plugin;
	private boolean cancelled;
}
