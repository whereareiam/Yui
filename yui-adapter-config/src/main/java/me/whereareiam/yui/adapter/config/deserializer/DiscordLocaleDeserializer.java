package me.whereareiam.yui.adapter.config.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DiscordLocaleDeserializer extends JsonDeserializer<DiscordLocale> {
	@Override
	public DiscordLocale deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		JsonNode node = parser.getCodec().readTree(parser);
		String value = node.asText();

		if (value == null || value.isEmpty())
			return DiscordLocale.UNKNOWN;

		return DiscordLocale.from(value);
	}
}