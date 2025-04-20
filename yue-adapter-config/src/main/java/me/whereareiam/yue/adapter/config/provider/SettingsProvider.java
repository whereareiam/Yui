package me.whereareiam.yue.adapter.config.provider;

import jakarta.annotation.PostConstruct;
import me.whereareiam.yue.adapter.config.management.ConfigLoader;
import me.whereareiam.yue.api.input.Registry;
import me.whereareiam.yue.api.model.config.settings.Settings;
import me.whereareiam.yue.api.output.Reloadable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class SettingsProvider implements Reloadable {
	private final Path dataPath;
	private final ConfigLoader configLoader;

	private Settings settings;

	@Autowired
	public SettingsProvider(@Qualifier("dataPath") Path dataPath,
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

	public Settings get() {
		if (settings == null) {
			load();
		}
		return settings;
	}

	@Override
	public void reload() {
		load();
	}

	private void load() {
		settings = configLoader.load(dataPath.resolve("settings"), Settings.class);
	}
}