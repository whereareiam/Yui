package me.whereareiam.yui.adapter.config.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;

@Component
public class ColorSerializer extends JsonSerializer<Color> {
	@Override
	public void serialize(Color value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		String hex = String.format("#%02X%02X%02X",
				value.getRed(),
				value.getGreen(),
				value.getBlue());
		gen.writeString(hex);
	}
}