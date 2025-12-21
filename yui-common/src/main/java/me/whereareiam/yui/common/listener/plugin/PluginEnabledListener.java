package me.whereareiam.yui.common.listener.plugin;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.common.scanner.ComponentListenerScanner;
import me.whereareiam.yui.common.scanner.ListenerScanner;
import me.whereareiam.yui.common.translation.loader.PluginTranslationLoader;
import me.whereareiam.yui.event.plugin.PluginEnabledEvent;
import me.whereareiam.yui.model.plugin.InternalPlugin;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PluginEnabledListener {
	private final ComponentListenerScanner componentScanner;
	private final ListenerScanner listenerScanner;
	private final PluginTranslationLoader pluginTranslationLoader;

	@EventListener
	public void onPluginEnabledEvent(PluginEnabledEvent event) {
		InternalPlugin plugin = event.getPlugin();

		listenerScanner.scan(plugin.getContext());
		componentScanner.scan(plugin.getContext());

		if (plugin.getPlugin().getName() != null)
			pluginTranslationLoader.loadPlugin(plugin.getPlugin().getName());
	}
}