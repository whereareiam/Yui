package me.whereareiam.yue.adapter.config.factory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yue.adapter.config.deserializer.ColorDeserializer;
import me.whereareiam.yue.adapter.config.deserializer.DiscordLocaleDeserializer;
import me.whereareiam.yue.adapter.config.serializer.ColorSerializer;
import me.whereareiam.yue.adapter.config.serializer.DiscordLocaleSerializer;
import me.whereareiam.yue.api.type.ConfigurationType;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
@RequiredArgsConstructor
public class ObjectMapperFactory {
	private final ConfigurationType configurationType;

	private final ColorDeserializer colorDeserializer;
	private final ColorSerializer colorSerializer;
	private final DiscordLocaleDeserializer discordLocaleDeserializer;
	private final DiscordLocaleSerializer discordLocaleSerializer;

	private ObjectMapper objectMapper;

	public ObjectMapper createObjectMapper() {
		if (objectMapper != null) return objectMapper;

		switch (configurationType) {
			case JSON -> objectMapper = new JsonMapper();
			case YAML -> {
				YAMLFactory yamlFactory = new YAMLFactory()
						.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
						.enable(YAMLGenerator.Feature.INDENT_ARRAYS)
						.enable(YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR)
						.disable(YAMLGenerator.Feature.SPLIT_LINES);
				objectMapper = new YAMLMapper(yamlFactory);
			}

			default -> throw new IllegalArgumentException("Unsupported configuration type");
		}

		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.registerModule(getColorModule());

		return objectMapper;
	}

	private SimpleModule getColorModule() {
		SimpleModule module = new SimpleModule();

		module.addDeserializer(Color.class, colorDeserializer);
		module.addSerializer(Color.class, colorSerializer);
		module.addDeserializer(DiscordLocale.class, discordLocaleDeserializer);
		module.addSerializer(DiscordLocale.class, discordLocaleSerializer);

		return module;
	}
}