package me.whereareiam.yui.common.config.provider.style;

import me.whereareiam.configura.Config;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yui.common.config.provider.DefaultConfigProvider;
import me.whereareiam.yui.common.config.template.style.PaletteTemplate;
import me.whereareiam.yui.model.config.style.Palette;
import me.whereareiam.yui.registry.Registry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class PaletteProvider extends DefaultConfigProvider<Palette> {
	@Autowired
	public PaletteProvider(
			@Qualifier("stylesPath") Path stylesPath,
			Registry<Reloadable> registry
	) {
		super(stylesPath, registry);
	}

	@Override
	protected Palette load() {
		return Config.update(getBasePath().resolve("palette"), Palette.class);
	}

	@Override
	protected void registerTemplate() {
		Config.registerTemplate(PaletteTemplate.class);
	}

	@Override
	public Class<Palette> getObjectType() {
		return Palette.class;
	}
}