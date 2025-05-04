package me.whereareiam.yui.adapter.config.template.style;

import me.whereareiam.yui.api.model.config.style.embed.Embed;
import me.whereareiam.yui.api.model.config.style.embed.EmbedStyle;
import me.whereareiam.yui.api.output.config.DefaultConfig;
import org.springframework.stereotype.Component;

@Component
public class EmbedsTemplate implements DefaultConfig<EmbedStyle> {
	@Override
	public EmbedStyle getDefault() {
		EmbedStyle embedStyle = new EmbedStyle();

		// Default values
		embedStyle.setPrimary(new Embed(
				new Embed.Author("", "", ""),
				new Embed.Footer("Yui", ""),
				"",
				"",
				"",
				true
		));

		embedStyle.setSecondary(new Embed(
				new Embed.Author("", "", ""),
				new Embed.Footer("", ""),
				"",
				"",
				"",
				false
		));

		embedStyle.setSuccess(new Embed(
				new Embed.Author("", "", ""),
				new Embed.Footer("", ""),
				"",
				"",
				"",
				false
		));

		embedStyle.setWarning(new Embed(
				new Embed.Author("", "", ""),
				new Embed.Footer("", ""),
				"",
				"",
				"",
				false
		));

		embedStyle.setError(new Embed(
				new Embed.Author("", "", ""),
				new Embed.Footer("", ""),
				"",
				"",
				"",
				false
		));

		embedStyle.setInfo(new Embed(
				new Embed.Author("", "", ""),
				new Embed.Footer("", ""),
				"",
				"",
				"",
				false
		));

		return embedStyle;
	}
}
