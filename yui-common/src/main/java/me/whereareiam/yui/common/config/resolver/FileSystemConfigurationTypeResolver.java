package me.whereareiam.yui.common.config.resolver;

import me.whereareiam.yui.config.ConfigurationTypeResolver;
import me.whereareiam.yui.type.ConfigurationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class FileSystemConfigurationTypeResolver implements ConfigurationTypeResolver {
	private final Path dataPath;

	@Autowired
	public FileSystemConfigurationTypeResolver(@Qualifier("dataPath") Path dataPath) {
		this.dataPath = dataPath;
	}

	@Override
	public ConfigurationType getConfigurationType() {
		try (Stream<Path> paths = Files.list(dataPath)) {
			Optional<Path> configFile = paths
					.filter(Files::isRegularFile)
					.filter(p -> p.getFileName().toString().startsWith("type="))
					.findFirst();

			if (configFile.isPresent()) {
				String[] parts = configFile.get().getFileName().toString().split("=", 2);
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
