package me.whereareiam.yui.common.service;

import me.whereareiam.yui.model.config.style.Palette;
import me.whereareiam.yui.model.config.style.embed.EmbedStyle;
import me.whereareiam.yui.registry.Registry;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yui.Provider;
import me.whereareiam.yui.style.StyleKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StyleKitReloadService implements Reloadable {
	private final Provider<Palette> palette;
	private final Provider<EmbedStyle> embedStyle;
	private final StyleKit styleKit;

	@Autowired
	public StyleKitReloadService(
			Provider<Palette> palette,
			Provider<EmbedStyle> embedStyle,
			Registry<Reloadable> reloadableRegistry,
			StyleKit styleKit
	) {
		this.palette = palette;
		this.embedStyle = embedStyle;
		this.styleKit = styleKit;

		reloadableRegistry.register(this);
	}

	@Override
	public void reload() {
		styleKit.init(palette.get(), embedStyle.get());
	}
}