package me.whereareiam.yui.common.config.template.style;

import me.whereareiam.configura.merge.defaults.DefaultsProvider;
import me.whereareiam.yui.model.config.style.StyleColor;
import me.whereareiam.yui.model.config.style.embed.Embed;
import me.whereareiam.yui.model.config.style.embed.EmbedStyle;
import org.springframework.stereotype.Component;

/**
 * Each embed kind in one of Yui's palette colours; the primary kind carries Yui's footer and a timestamp.
 */
@Component
public class EmbedsTemplate implements DefaultsProvider<EmbedStyle> {
	@Override
	public EmbedStyle supply(EmbedStyle style) {
		Embed primary = kind("brand");
		Embed.Footer footer = new Embed.Footer();
		footer.setText("Yui");
		primary.setFooter(footer);
		primary.setTimestamp(true);

		style.setPrimary(primary);
		style.setSecondary(kind("sand"));
		style.setSuccess(kind("green"));
		style.setWarning(kind("amber"));
		style.setError(kind("red"));
		style.setInfo(kind("blue"));

		return style;
	}

	private static Embed kind(String color) {
		Embed embed = new Embed();
		embed.setColor(StyleColor.named(color));

		return embed;
	}
}
