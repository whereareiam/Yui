package me.whereareiam.yue.adapter.config.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.whereareiam.yue.api.exception.ConfigLoadException;
import me.whereareiam.yue.api.output.config.ConfigurationLoader;
import me.whereareiam.yue.api.output.config.ConfigurationManager;
import me.whereareiam.yue.api.type.ConfigurationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

@Component
public class ConfigLoader implements ConfigurationLoader {
	private final ConfigurationManager configManager;
	private final ConfigurationType configurationType;
	private final ObjectMapper objectMapper;

	private final ConfigSaver configSaver;
	private final ConfigMerger configMerger;

	@Autowired
	public ConfigLoader(ConfigurationManager configManager,
	                    ConfigurationType configurationType,
	                    ObjectMapper objectMapper,
	                    ConfigSaver configSaver,
	                    ConfigMerger configMerger) {
		this.configManager = configManager;
		this.configurationType = configurationType;
		this.objectMapper = objectMapper;
		this.configSaver = configSaver;
		this.configMerger = configMerger;
	}

	@SuppressWarnings("unchecked")
	public <T> T load(Path path, Class<T> clazz) {
		path = path.resolveSibling(path.getFileName() + configurationType.getExtension());

		T config;
		try {
			config = objectMapper.readValue(path.toFile(), clazz);
		} catch (FileNotFoundException e) {
			config = configManager.getTemplate(clazz).getDefault();
			configSaver.save(path, config);
		} catch (Exception e) {
			throw new ConfigLoadException("Failed to load configuration", e);
		}

		T defaultConfig = configManager.getTemplate(clazz).getDefault();
		configMerger.merge(config, defaultConfig);
		configSaver.save(path, config);

		return config;
	}

	@Override
	public <T> T load(InputStream stream, Class<T> clazz) {
		T config;
		try {
			config = objectMapper.readValue(stream, clazz);
		} catch (Exception e) {
			throw new ConfigLoadException("Failed to load configuration", e);
		}

		return config;
	}
}