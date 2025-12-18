package me.whereareiam.yui.common.config.provider;

import me.whereareiam.configura.Config;
import me.whereareiam.yui.common.config.template.CommandsTemplate;
import me.whereareiam.yui.model.config.Commands;
import org.springframework.stereotype.Component;

@Component
public class CommandsProvider extends DefaultConfigProvider<Commands> {
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
}
