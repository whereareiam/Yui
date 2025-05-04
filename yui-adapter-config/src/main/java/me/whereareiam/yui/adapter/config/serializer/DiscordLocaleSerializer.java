package me.whereareiam.yui.adapter.config.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DiscordLocaleSerializer extends JsonSerializer<DiscordLocale> {
	@Override
	public void serialize(DiscordLocale value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		if (value == null) {
			gen.writeNull();
			return;
		}

		gen.writeString(value.getLocale());
	}
}