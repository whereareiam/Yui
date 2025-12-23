package me.whereareiam.yui.common.listener.plugin;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.command.CommandService;
import me.whereareiam.yui.command.DefinitionProvider;
import me.whereareiam.yui.common.scanner.ComponentListenerScanner;
import me.whereareiam.yui.common.scanner.ListenerScanner;
import me.whereareiam.yui.event.plugin.PluginEnabledEvent;
import me.whereareiam.yui.model.plugin.InternalPlugin;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class PluginEnabledListener {
	private final ComponentListenerScanner componentScanner;
	private final ListenerScanner listenerScanner;
	private final CommandService commandService;

	@EventListener
	public void onPluginEnabledEvent(PluginEnabledEvent event) {
		InternalPlugin plugin = event.getPlugin();

		listenerScanner.scan(plugin.getContext());
		componentScanner.scan(plugin.getContext());
		
		// Register definition providers from plugin context
		plugin.getContext().getBeansOfType(DefinitionProvider.class).values()
				.forEach(provider -> {
					log.debug("Registering definition provider: {} from plugin: {}", 
							provider.id(), plugin.getPlugin().getId());
					commandService.registerProvider(provider);
				});
		
		log.debug("Registering commands for plugin: {}", plugin.getPlugin().getId());
		commandService.register(plugin.getContext());
	}
}