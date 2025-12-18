package me.whereareiam.yui.common.config.provider.style;

import me.whereareiam.configura.Config;
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
	@Qualifier("stylesPath")
	private Path stylesPath;

	@Override
	protected Path getBasePath() {
		return stylesPath;
	}

	@Override
	protected EmbedStyle load() {
		return Config.update(getBasePath().resolve("embeds"), EmbedStyle.class);
	}

	@Override
	protected void registerTemplate() {
		Config.registerTemplate(EmbedsTemplate.class);
	}

	@Override
	public Class<EmbedStyle> getObjectType() {
		return EmbedStyle.class;
	}
}
