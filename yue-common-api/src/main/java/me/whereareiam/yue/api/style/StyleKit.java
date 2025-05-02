package me.whereareiam.yue.api.style;

import me.whereareiam.yue.api.model.config.style.Palette;
import me.whereareiam.yue.api.model.config.style.embed.EmbedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@SuppressWarnings("unused")
public final class StyleKit {
	private static Palette palette;
	private static Embeds embeds;

	@Autowired
	public synchronized void init(Palette palette, EmbedStyle embeds) {
		StyleKit.palette = Objects.requireNonNull(palette, "Palette must not be null");
		StyleKit.embeds = new Embeds(StyleKit.palette, Objects.requireNonNull(embeds, "EmbedStyle must not be null"));
	}

	private static void requireInit() {
		if (palette == null || embeds == null)
			throw new IllegalStateException("StyleKit is not initialized");
	}

	public static Palette palette() {
		requireInit();
		return palette;
	}

	public static Embeds embeds() {
		requireInit();
		return embeds;
	}
}