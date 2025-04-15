package me.whereareiam.yue.adapter.config.provider;

import jakarta.annotation.PostConstruct;
import me.whereareiam.yue.adapter.config.management.ConfigLoader;
import me.whereareiam.yue.api.Reloadable;
import me.whereareiam.yue.api.input.Registry;
import me.whereareiam.yue.api.model.config.Commands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class CommandsProvider implements Reloadable {
	private final Path dataPath;
	private final ConfigLoader configLoader;

	private Commands commands;

	@Autowired
	public CommandsProvider(@Qualifier("dataPath") Path dataPath,
	                        ConfigLoader configLoader,
	                        Registry<Reloadable> registry) {
		this.dataPath = dataPath;
		this.configLoader = configLoader;

		registry.register(this);
	}

	@PostConstruct
	public void init() {
		load();
	}

	public Commands get() {
		if (commands == null) {
			load();
		}
		return commands;
	}

	@Override
	public void reload() {
		load();
	}

	private void load() {
		commands = configLoader.load(dataPath.resolve("commands"), Commands.class);
	}
}