package me.whereareiam.yui.common.config.adapter;

import me.whereareiam.configura.TypeAdapter;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class ColorAdapter implements TypeAdapter<Color> {
	@Override
	public Color deserialize(String value) {
		if (value == null || value.isEmpty()) return null;
		return Color.decode(value);
	}

	@Override
	public String serialize(Color value) {
		if (value == null) return null;
		return String.format("#%02X%02X%02X",
				value.getRed(),
				value.getGreen(),
				value.getBlue());
	}
}

