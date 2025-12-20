package me.whereareiam.yui.common.config.provider;

import me.whereareiam.configura.Config;
import me.whereareiam.yui.command.DefinitionProvider;
import me.whereareiam.yui.common.config.template.CommandsTemplate;
import me.whereareiam.yui.model.command.CommandDefinition;
import me.whereareiam.yui.model.config.Commands;
import me.whereareiam.yui.type.Source;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CommandsProvider extends DefaultConfigProvider<Commands> implements DefinitionProvider {
	@Override
	protected Commands load() {
		return Config.update(getBasePath().resolve("commands"), Commands.class);
	}

	@Override
	protected void registerTemplate() {
		Config.registerTemplate(CommandsTemplate.class);
	}

	@Override
	public Class<Commands> getObjectType() {
		return Commands.class;
	}

	@Override
	public @NotNull String id() {
		return "internal";
	}

	@Override
	public @NotNull Source source() {
		return Source.INTERNAL;
	}

	@Override
	public @NotNull Map<String, CommandDefinition> definitions() {
		return this.get().getCommands();
	}
}
