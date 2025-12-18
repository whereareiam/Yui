package me.whereareiam.yui.common.service;

import me.whereareiam.yui.model.config.style.Palette;
import me.whereareiam.yui.model.config.style.embed.EmbedStyle;
import me.whereareiam.yui.registry.Registry;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yui.style.StyleKit;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StyleKitReloadService implements Reloadable {
	private final ObjectProvider<Palette> palette;
	private final ObjectProvider<EmbedStyle> embedStyle;
	private final StyleKit styleKit;

	@Autowired
	public StyleKitReloadService(
			ObjectProvider<Palette> palette,
			ObjectProvider<EmbedStyle> embedStyle,
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
		styleKit.init(palette.getObject(), embedStyle.getObject());
	}
}
