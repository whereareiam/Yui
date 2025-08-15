package me.whereareiam.yui.common.listener.plugin;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.api.event.plugin.PluginEnabledEvent;
import me.whereareiam.yui.api.model.plugin.InternalPlugin;
import me.whereareiam.yui.common.scanner.ComponentListenerScanner;
import me.whereareiam.yui.common.scanner.ListenerScanner;
import me.whereareiam.yui.common.service.DefaultTranslationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PluginEnabledListener {
	private final ComponentListenerScanner componentScanner;
	private final ListenerScanner listenerScanner;
	private final DefaultTranslationService translations;

	@EventListener
	public void onPluginEnabledEvent(PluginEnabledEvent event) {
		InternalPlugin plugin = event.getPlugin();

		listenerScanner.scan(plugin.getContext());
		componentScanner.scan(plugin.getContext());

		if (plugin.getPlugin().getName() != null)
			translations.addTranslations(plugin.getPlugin().getName());
	}
}