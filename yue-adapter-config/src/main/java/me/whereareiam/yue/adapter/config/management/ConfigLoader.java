package me.whereareiam.yue.adapter.config.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import me.whereareiam.yue.api.exception.ConfigLoadException;
import me.whereareiam.yue.api.output.config.ConfigurationLoader;
import me.whereareiam.yue.api.output.config.ConfigurationManager;
import me.whereareiam.yue.api.output.config.DefaultConfig;
import me.whereareiam.yue.api.type.ConfigurationType;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

@Component
@AllArgsConstructor
public class ConfigLoader implements ConfigurationLoader {
	private final ConfigurationManager configManager;
	private final ConfigurationType configurationType;
	private final ObjectMapper objectMapper;
	private final ConfigSaver configSaver;
	private final ConfigMerger configMerger;

	@Override
	public <T> T load(Path path, Class<T> clazz) {
		DefaultConfig<T> defaultConfig = configManager.getTemplate(clazz);

		return loadWithDefault(path, clazz, defaultConfig);
	}

	@Override
	public <T> T load(InputStream stream, Class<T> clazz) {
		try {
			return objectMapper.readValue(stream, clazz);
		} catch (Exception e) {
			throw new ConfigLoadException("Failed to load configuration from stream", e);
		}
	}

	@Override
	public <T> T load(Path path, Class<T> clazz, DefaultConfig<T> defaultConfig) {
		return loadWithDefault(path, clazz, defaultConfig);
	}

	private <T> T loadWithDefault(Path path, Class<T> clazz, DefaultConfig<T> defaultConfig) {
		Path configPath = resolveConfigPath(path);
		T config;

		try {
			config = objectMapper.readValue(configPath.toFile(), clazz);
		} catch (FileNotFoundException e) {
			config = defaultConfig.getDefault();
			configSaver.save(configPath, config);
			return config;
		} catch (Exception e) {
			throw new ConfigLoadException("Failed to load configuration from " + configPath, e);
		}

		configMerger.merge(config, defaultConfig);
		configSaver.save(configPath, config);

		return config;
	}

	private Path resolveConfigPath(Path path) {
		return path.resolveSibling(path.getFileName() + configurationType.getExtension());
	}
}