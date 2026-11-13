package me.whereareiam.yui.common.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import me.whereareiam.configura.Configura;
import me.whereareiam.yui.api.config.ConfigReader;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads plugins' configuration through Yui's own Configura instance, so their files know Yui's value types.
 */
@Component
@RequiredArgsConstructor
public class ConfiguraReader implements ConfigReader {
	private final Configura configura;

	@Override
	public <T> @NotNull T read(@NotNull Path file, @NotNull T defaults) {
		return configura.merge(file.toString(), defaults);
	}

	@Override
	public <V> @NotNull Map<String, V> readMap(@NotNull Path file, @NotNull Class<V> value) {
		Path written = Path.of(file + configura.extension());
		if (!Files.exists(written)) return Map.of();

		Map<String, V> entries = new LinkedHashMap<>();
		configura.readNode(written).properties().forEach(entry -> entries.put(entry.getKey(), convert(entry.getKey(), entry.getValue(), value)));

		return entries;
	}

	private <V> V convert(String key, JsonNode node, Class<V> value) {
		try {
			return configura.mapper().treeToValue(node, value);
		} catch (JsonProcessingException exception) {
			throw new IllegalArgumentException("The entry " + key + " does not fit " + value.getSimpleName() + ": " + exception.getOriginalMessage(), exception);
		}
	}

	@Override
	public <T> @NotNull T parse(@NotNull String yaml, @NotNull Class<T> type) {
		try {
			return configura.mapper().readValue(yaml, type);
		} catch (JsonProcessingException exception) {
			throw new IllegalArgumentException("The value does not fit " + type.getSimpleName() + ": " + exception.getOriginalMessage(), exception);
		}
	}
}
