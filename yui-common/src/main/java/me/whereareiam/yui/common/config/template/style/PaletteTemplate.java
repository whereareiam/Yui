package me.whereareiam.yui.common.config.template.style;

import me.whereareiam.configura.merge.defaults.DefaultsProvider;
import me.whereareiam.yui.api.model.config.style.Palette;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Yui's own colours. A palette file adds to them or changes them.
 */
@Component
public class PaletteTemplate implements DefaultsProvider<Palette> {
	@Override
	public Palette supply(Palette palette) {
		Map<String, Color> colors = new LinkedHashMap<>();
		colors.put("brand", Color.decode("#B57E3F"));
		colors.put("sand", Color.decode("#E6CBA8"));
		colors.put("green", Color.decode("#00FF00"));
		colors.put("amber", Color.decode("#FFAA00"));
		colors.put("red", Color.decode("#FF4C4C"));
		colors.put("blue", Color.decode("#00AAFF"));

		return new Palette(colors);
	}
}
