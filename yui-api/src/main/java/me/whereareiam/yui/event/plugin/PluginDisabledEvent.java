package me.whereareiam.yui.event.plugin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.whereareiam.yui.event.Cancellable;
import me.whereareiam.yui.event.plugin.base.PluginLifecycleEvent;
import me.whereareiam.yui.model.plugin.InternalPlugin;

@Getter
@Setter
@RequiredArgsConstructor
public class PluginDisabledEvent implements PluginLifecycleEvent, Cancellable {
	private final InternalPlugin plugin;
	private boolean cancelled;
}
