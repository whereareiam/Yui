package me.whereareiam.yui.adapter.config.provider.style;

import jakarta.annotation.PostConstruct;
import me.whereareiam.yui.adapter.config.management.ConfigLoader;
import me.whereareiam.yui.api.input.Registry;
import me.whereareiam.yui.api.model.config.style.embed.EmbedStyle;
import me.whereareiam.yui.api.output.Reloadable;
import me.whereareiam.yui.api.output.provider.Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class EmbedsProvider implements Provider<EmbedStyle>, Reloadable {
	private final Path stylesPath;
	private final ConfigLoader configLoader;

	private EmbedStyle embedStyle;

	@Autowired
	public EmbedsProvider(@Qualifier("stylesPath") Path stylesPath,
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
	public EmbedStyle get() {
		if (embedStyle == null) {
			load();
		}
		return embedStyle;
	}

	@Override
	public void reload() {
		load();
	}

	private void load() {
		embedStyle = configLoader.load(stylesPath.resolve("embeds"), EmbedStyle.class);
	}
}