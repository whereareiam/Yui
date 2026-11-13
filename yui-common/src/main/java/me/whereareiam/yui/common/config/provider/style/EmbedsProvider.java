package me.whereareiam.yui.common.config.provider.style;

import me.whereareiam.yui.common.config.provider.DefaultConfigProvider;
import me.whereareiam.yui.common.config.template.style.EmbedsTemplate;
import me.whereareiam.yui.model.config.style.embed.EmbedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class EmbedsProvider extends DefaultConfigProvider<EmbedStyle> {
	@Autowired
	private EmbedsTemplate template;

	@Autowired
	@Qualifier("stylesPath")
	private Path stylesPath;

	@Override
	protected Path getBasePath() {
		return stylesPath;
	}

	@Override
	protected EmbedStyle load() {
		return update("embeds", template.supply(new EmbedStyle()));
	}

	@Override
	public Class<EmbedStyle> getObjectType() {
		return EmbedStyle.class;
	}
}
