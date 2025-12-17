package me.whereareiam.yui.adapter.config.initializer;

import me.whereareiam.yui.adapter.config.template.CommandsTemplate;
import me.whereareiam.yui.model.config.Commands;
import me.whereareiam.yui.config.ConfigurationLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class CommandsInitializer {
	@Autowired
	public CommandsInitializer(@Qualifier("dataPath") Path dataPath, ConfigurationLoader configLoader) {
		configLoader.load(dataPath.resolve("commands"), Commands.class, new CommandsTemplate());
	}
}
