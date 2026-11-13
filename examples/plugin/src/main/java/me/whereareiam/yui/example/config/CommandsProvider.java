package me.whereareiam.yui.example.config;

import me.whereareiam.conditia.model.Requirements;
import me.whereareiam.yui.api.command.DefinitionProvider;
import me.whereareiam.yui.api.config.PluginConfigProvider;
import me.whereareiam.yui.api.model.command.CommandCooldown;
import me.whereareiam.yui.api.model.command.CommandDefinition;
import me.whereareiam.yui.api.model.config.Commands;
import me.whereareiam.yui.api.model.type.Duration;
import me.whereareiam.yui.api.type.CommandCategory;
import me.whereareiam.yui.api.type.Source;
import me.whereareiam.yui.example.Greeting;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Reads {@code commands.yml}, which may rename, restrict or switch off {@code /hello}.
 */
@Component
public class CommandsProvider extends PluginConfigProvider<Commands> implements DefinitionProvider {
	@Override
	protected Commands load() {
		Commands defaults = new Commands();
		defaults.getCommands().put("hello", new CommandDefinition(
				true,
				List.of("hello"),
				"{alias}",
				CommandCategory.FUN,
				new CommandCooldown(true, Duration.ofSeconds(5), null),
				Requirements.none(),
				false
		));

		return read("commands", defaults);
	}

	@Override
	public Class<Commands> getObjectType() {
		return Commands.class;
	}

	@Override
	public @NotNull String id() {
		return Greeting.ID;
	}

	@Override
	public @NotNull Source source() {
		return Source.EXTERNAL;
	}

	@Override
	public @NotNull Map<String, CommandDefinition> definitions() {
		return get().getCommands();
	}
}
