package me.whereareiam.yui.common.plugin.part;

import lombok.RequiredArgsConstructor;
import me.whereareiam.chassis.ExtensionContext;
import me.whereareiam.yui.command.CommandService;
import me.whereareiam.yui.command.DefinitionProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * A plugin's definition providers and the commands of its context.
 */
@Component
@Order(4)
@RequiredArgsConstructor
class CommandsPart implements PluginPart {
	private final CommandService commands;

	@Override
	public AutoCloseable attach(ApplicationContext plugin, ExtensionContext extension) {
		List<DefinitionProvider> providers = List.copyOf(plugin.getBeansOfType(DefinitionProvider.class).values());
		providers.forEach(commands::registerProvider);
		commands.register(plugin);

		return () -> {
			commands.unregisterByContext(plugin);
			providers.forEach(provider -> commands.unregisterProvider(provider.id()));
		};
	}
}
