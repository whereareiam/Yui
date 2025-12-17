package me.whereareiam.yui.common.config.template.style;

import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.yui.model.config.style.Palette;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class PaletteTemplate implements TemplateProvider<Palette> {
	@Override
	public Palette supply(Palette palette) {
		// Default values
		palette.setPrimary(Color.decode("#B57E3F"));
		palette.setSecondary(Color.decode("#E6CBA8"));

		palette.setError(Color.decode("#FF4C4C"));
		palette.setWarning(Color.decode("#FFAA00"));
		palette.setSuccess(Color.decode("#00FF00"));
		palette.setInfo(Color.decode("#00AAFF"));

		return palette;
	}
}
