package me.whereareiam.yui.adapter.config.provider.style;

import jakarta.annotation.PostConstruct;
import me.whereareiam.yui.adapter.config.management.ConfigLoader;
import me.whereareiam.yui.registry.Registry;
import me.whereareiam.yui.model.config.style.Palette;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yui.Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class PaletteProvider implements Provider<Palette>, Reloadable {
	private final Path stylesPath;
	private final ConfigLoader configLoader;

	private Palette palette;

	@Autowired
	public PaletteProvider(@Qualifier("stylesPath") Path stylesPath,
	                       ConfigLoader configLoader,
	                       Registry<Reloadable> registry) {
		this.stylesPath = stylesPath;
		this.configLoader = configLoader;

		registry.register(this);
	}

	@PostConstruct
	public void init() {
		load();
	}

	@Override
	public Palette get() {
		if (palette == null) {
			load();
		}
		return palette;
	}

	@Override
	public void reload() {
		load();
	}

	private void load() {
		palette = configLoader.load(stylesPath.resolve("palette"), Palette.class);
	}
}