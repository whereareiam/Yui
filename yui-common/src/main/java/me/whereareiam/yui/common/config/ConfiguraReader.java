package me.whereareiam.yui.common.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import me.whereareiam.configura.Configura;
import me.whereareiam.yui.api.config.ConfigReader;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

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
	public <T> @NotNull T parse(@NotNull String yaml, @NotNull Class<T> type) {
		try {
			return configura.mapper().readValue(yaml, type);
		} catch (JsonProcessingException exception) {
			throw new IllegalArgumentException("The value does not fit " + type.getSimpleName() + ": " + exception.getOriginalMessage(), exception);
		}
	}
}
