package me.whereareiam.yue.common.service;

import me.whereareiam.yue.api.model.config.style.Palette;
import me.whereareiam.yue.api.model.config.style.embed.EmbedStyle;
import me.whereareiam.yue.api.output.Reloadable;
import me.whereareiam.yue.api.output.provider.Provider;
import me.whereareiam.yue.api.style.StyleKit;
import me.whereareiam.yue.common.provider.ReloadableProvider;
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
			ReloadableProvider reloadableProvider,
			StyleKit styleKit
	) {
		this.palette = palette;
		this.embedStyle = embedStyle;
		this.styleKit = styleKit;

		reloadableProvider.register(this);
	}

	@Override
	public void reload() {
		styleKit.init(palette.get(), embedStyle.get());
	}
}