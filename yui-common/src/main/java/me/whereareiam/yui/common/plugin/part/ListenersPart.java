package me.whereareiam.yui.common.plugin.part;

import lombok.RequiredArgsConstructor;
import me.whereareiam.chassis.ExtensionContext;
import me.whereareiam.yui.common.scanner.ComponentListenerScanner;
import me.whereareiam.yui.common.scanner.ListenerScanner;
import me.whereareiam.yui.service.InteractionService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * A plugin's JDA listeners and its component listeners (buttons, menus), whose paths carry its id.
 */
@Component
@Order(3)
@RequiredArgsConstructor
class ListenersPart implements PluginPart {
	private final JDA jda;
	private final ListenerScanner listeners;
	private final ComponentListenerScanner components;
	private final InteractionService interactions;

	@Override
	public AutoCloseable attach(ApplicationContext plugin, ExtensionContext extension) {
		List<ListenerAdapter> adapters = List.copyOf(plugin.getBeansOfType(ListenerAdapter.class).values());
		listeners.scan(plugin);
		components.scan(plugin);

		return () -> {
			adapters.forEach(jda::removeEventListener);
			interactions.unregister(extension.descriptor().getId());
		};
	}
}
