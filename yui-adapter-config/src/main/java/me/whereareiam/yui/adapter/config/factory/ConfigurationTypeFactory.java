package me.whereareiam.yui.adapter.config.factory;

import me.whereareiam.yui.api.type.ConfigurationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class ConfigurationTypeFactory {
	private final Path dataPath;

	@Autowired
	public ConfigurationTypeFactory(@Qualifier("dataPath") Path dataPath) {
		this.dataPath = dataPath;
	}

	public ConfigurationType getConfigurationType() {
		try (Stream<Path> paths = Files.list(dataPath)) {
			Optional<Path> configFile = paths
					.filter(Files::isRegularFile)
					.filter(file -> file.getFileName().toString().startsWith("type"))
					.findFirst();

			if (configFile.isPresent()) {
				String[] parts = configFile.get().getFileName().toString().split("=");

				return ConfigurationType.valueOf(parts[1].toUpperCase());
			} else {
				Files.createFile(dataPath.resolve("type=YAML"));

				return ConfigurationType.YAML;
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to get configuration type", e);
		}
	}
}
