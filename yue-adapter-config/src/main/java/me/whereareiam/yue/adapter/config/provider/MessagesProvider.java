package me.whereareiam.yue.adapter.config.provider;

import jakarta.annotation.PostConstruct;
import me.whereareiam.yue.adapter.config.management.ConfigLoader;
import me.whereareiam.yue.api.Reloadable;
import me.whereareiam.yue.api.input.Registry;
import me.whereareiam.yue.api.model.config.messages.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class MessagesProvider implements Reloadable {
	private final Path dataPath;
	private final ConfigLoader configLoader;

	private Messages messages;

	@Autowired
	public MessagesProvider(@Qualifier("languagesPath") Path languagesPath,
	                        ConfigLoader configLoader,
	                        Registry<Reloadable> registry) {
		this.dataPath = languagesPath;
		this.configLoader = configLoader;

		registry.register(this);
	}

	@PostConstruct
	public void init() {
		load();
	}

	public Messages get() {
		if (messages == null) {
			load();
		}
		return messages;
	}

	@Override
	public void reload() {
		load();
	}

	private void load() {
		messages = configLoader.load(dataPath.resolve("en"), Messages.class);
	}
}