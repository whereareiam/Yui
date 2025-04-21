package me.whereareiam.yue.adapter.config.template.style;

import me.whereareiam.yue.api.model.config.style.Palette;
import me.whereareiam.yue.api.output.config.DefaultConfig;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class PaletteTemplate implements DefaultConfig<Palette> {
	@Override
	public Palette getDefault() {
		Palette palette = new Palette();

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
