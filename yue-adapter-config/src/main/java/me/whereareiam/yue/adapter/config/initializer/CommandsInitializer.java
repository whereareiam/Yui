package me.whereareiam.yue.adapter.config.initializer;

import me.whereareiam.yue.adapter.config.template.CommandsTemplate;
import me.whereareiam.yue.api.model.config.Commands;
import me.whereareiam.yue.api.output.config.ConfigurationLoader;
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
