package me.whereareiam.yui.common.journey;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.common.scanner.JourneyAnnotationScanner;
import me.whereareiam.yui.event.plugin.PluginDisabledEvent;
import me.whereareiam.yui.event.plugin.PluginEnabledEvent;
import me.whereareiam.yui.event.plugin.PluginUnloadedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JourneyLifecycle {
	private final JourneyAnnotationScanner journeyScanner;
	private final JourneyDefinitionRegistry journeyDefinitionRegistry;

	@EventListener
	public void onPluginEnabled(PluginEnabledEvent event) {
		journeyScanner.scan(event.getPlugin().getContext());
	}

	@EventListener
	public void onPluginDisabled(PluginDisabledEvent event) {
		journeyDefinitionRegistry.unregisterByContext(event.getPlugin().getContext());
	}

	@EventListener
	public void onPluginUnloaded(PluginUnloadedEvent event) {
		journeyDefinitionRegistry.unregisterByContext(event.getPlugin().getContext());
	}
}
