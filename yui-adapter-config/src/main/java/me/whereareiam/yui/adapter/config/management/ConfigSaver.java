package me.whereareiam.yui.adapter.config.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.whereareiam.yui.config.ConfigurationSaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class ConfigSaver implements ConfigurationSaver {
	private final ObjectMapper objectMapper;

	@Autowired
	public ConfigSaver(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public <T> void save(Path path, T object) {
		try {
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), object);
		} catch (Exception e) {
			throw new RuntimeException("Failed to save configuration", e);
		}
	}
}