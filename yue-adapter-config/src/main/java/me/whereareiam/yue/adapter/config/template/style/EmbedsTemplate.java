package me.whereareiam.yue.adapter.config.template.style;

import me.whereareiam.yue.api.model.config.style.embed.Embed;
import me.whereareiam.yue.api.model.config.style.embed.EmbedStyle;
import me.whereareiam.yue.api.output.config.DefaultConfig;
import org.springframework.stereotype.Component;

@Component
public class EmbedsTemplate implements DefaultConfig<EmbedStyle> {
	@Override
	public EmbedStyle getDefault() {
		EmbedStyle embedStyle = new EmbedStyle();

		// Default values
		embedStyle.setPrimary(new Embed(
				new Embed.Author("", "", ""),
				new Embed.Footer("Yue", ""),
				"",
				"",
				"",
				true
		));

		return embedStyle;
	}
}
