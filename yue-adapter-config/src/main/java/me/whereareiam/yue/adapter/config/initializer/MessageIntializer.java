package me.whereareiam.yue.adapter.config.initializer;

import me.whereareiam.yue.api.model.config.messages.Messages;
import me.whereareiam.yue.api.output.ResourceInitializer;
import me.whereareiam.yue.api.output.config.ConfigurationLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class MessageIntializer implements ResourceInitializer {
	private final Path languagesPath;
	private final ConfigurationLoader configLoader;

	@Autowired
	public MessageIntializer(@Qualifier("languagesPath") Path languagesPath, ConfigurationLoader configLoader) {
		this.languagesPath = languagesPath;
		this.configLoader = configLoader;
	}

	@Override
	public void initialize() {
		configLoader.load(languagesPath.resolve("en"), Messages.class);
	}
}
