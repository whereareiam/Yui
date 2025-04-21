package me.whereareiam.yue.adapter.config.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;

@Component
public class ColorDeserializer extends JsonDeserializer<Color> {
	@Override
	public Color deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		String hex = p.getText();
		return Color.decode(hex);
	}
}