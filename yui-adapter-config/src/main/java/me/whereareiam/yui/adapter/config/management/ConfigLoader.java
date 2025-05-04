package me.whereareiam.yui.adapter.config.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import me.whereareiam.yui.api.exception.ConfigLoadException;
import me.whereareiam.yui.api.output.config.ConfigurationLoader;
import me.whereareiam.yui.api.output.config.ConfigurationManager;
import me.whereareiam.yui.api.output.config.DefaultConfig;
import me.whereareiam.yui.api.type.ConfigurationType;
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

		if (defaultConfig.getDefault() != null)
			configMerger.merge(config, defaultConfig.getDefault());
		
		configSaver.save(configPath, config);

		return config;
	}

	private Path resolveConfigPath(Path path) {
		return path.resolveSibling(path.getFileName() + configurationType.getExtension());
	}
}