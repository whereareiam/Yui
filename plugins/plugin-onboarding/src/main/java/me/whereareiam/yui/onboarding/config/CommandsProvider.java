package me.whereareiam.yui.onboarding.config;

import me.whereareiam.conditia.model.Requirements;
import me.whereareiam.yui.api.command.DefinitionProvider;
import me.whereareiam.yui.api.config.PluginConfigProvider;
import me.whereareiam.yui.api.model.command.CommandCooldown;
import me.whereareiam.yui.api.model.command.CommandDefinition;
import me.whereareiam.yui.api.model.config.Commands;
import me.whereareiam.yui.api.model.type.Duration;
import me.whereareiam.yui.api.type.CommandCategory;
import me.whereareiam.yui.api.type.Source;
import me.whereareiam.yui.onboarding.Onboarding;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Reads {@code commands.yml}: {@code /verify <user>}, for moderators, starts onboarding by hand.
 */
@Component
public class CommandsProvider extends PluginConfigProvider<Commands> implements DefinitionProvider {
	@Override
	protected Commands load() {
		Commands defaults = new Commands();
		defaults.getCommands().put("verify", new CommandDefinition(
				true,
				List.of("verify"),
				"{command} {alias} <user>",
				CommandCategory.ADMINISTRATION,
				new CommandCooldown(false, Duration.ofSeconds(10), null),
				reader().parse("- audience: moderator", Requirements.class),
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
		return Onboarding.ID;
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
