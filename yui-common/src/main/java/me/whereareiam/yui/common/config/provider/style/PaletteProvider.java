package me.whereareiam.yui.common.config.provider.style;

import me.whereareiam.yui.common.config.provider.DefaultConfigProvider;
import me.whereareiam.yui.common.config.template.style.PaletteTemplate;
import me.whereareiam.yui.model.config.style.Palette;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class PaletteProvider extends DefaultConfigProvider<Palette> {
	@Autowired
	private PaletteTemplate template;

	@Autowired
	@Qualifier("stylesPath")
	private Path stylesPath;

	@Override
	protected Path getBasePath() {
		return stylesPath;
	}

	@Override
	protected Palette load() {
		return read("palette", template.supply(new Palette()));
	}

	@Override
	public Class<Palette> getObjectType() {
		return Palette.class;
	}
}
