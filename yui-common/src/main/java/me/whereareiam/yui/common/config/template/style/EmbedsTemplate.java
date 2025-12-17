package me.whereareiam.yui.common.config.template.style;

import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.yui.model.config.style.embed.Embed;
import me.whereareiam.yui.model.config.style.embed.EmbedStyle;
import org.springframework.stereotype.Component;

@Component
public class EmbedsTemplate implements TemplateProvider<EmbedStyle> {
	@Override
	public EmbedStyle supply(EmbedStyle embedStyle) {
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
