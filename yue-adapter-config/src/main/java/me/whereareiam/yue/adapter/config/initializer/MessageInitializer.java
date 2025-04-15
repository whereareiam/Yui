package me.whereareiam.yue.adapter.config.initializer;

import me.whereareiam.yue.api.model.config.messages.Messages;
import me.whereareiam.yue.api.output.config.ConfigurationLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class MessageInitializer {
	@Autowired
	public MessageInitializer(@Qualifier("languagesPath") Path languagesPath, ConfigurationLoader configLoader) {
		configLoader.load(languagesPath.resolve("en"), Messages.class);
	}
}
