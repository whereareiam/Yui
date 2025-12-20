package me.whereareiam.yui.common.config.provider;

import me.whereareiam.configura.Config;
import me.whereareiam.yui.common.config.template.LanguagesTemplate;
import me.whereareiam.yui.model.config.languages.Languages;
import org.springframework.stereotype.Component;

@Component
public class LanguagesProvider extends DefaultConfigProvider<Languages> {
	@Override
	protected Languages load() {
		return Config.update(getBasePath().resolve("languages"), Languages.class);
	}

	@Override
	protected void registerTemplate() {
		Config.registerTemplate(LanguagesTemplate.class);
	}

	@Override
	public Class<Languages> getObjectType() {
		return Languages.class;
	}
}

