package me.whereareiam.yue.common.service;

import me.whereareiam.yue.api.StyleKit;
import me.whereareiam.yue.api.model.config.style.Palette;
import me.whereareiam.yue.api.model.config.style.embed.EmbedStyle;
import me.whereareiam.yue.api.output.Reloadable;
import me.whereareiam.yue.api.output.provider.Provider;
import me.whereareiam.yue.common.provider.ReloadableProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StyleKitReloadService implements Reloadable {
	private final Provider<Palette> palette;
	private final Provider<EmbedStyle> embedStyle;

	@Autowired
	public StyleKitReloadService(Provider<Palette> palette,
	                             Provider<EmbedStyle> embedStyle,
	                             ReloadableProvider reloadableProvider
	) {
		this.palette = palette;
		this.embedStyle = embedStyle;
		reloadableProvider.register(this);
	}

	@Override
	public void reload() {
		StyleKit.init(palette.get(), embedStyle.get());
	}
}