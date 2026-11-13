package me.whereareiam.yui.common.config.provider;

import me.whereareiam.yui.common.config.template.LanguagesTemplate;
import me.whereareiam.yui.model.config.languages.Languages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LanguagesProvider extends DefaultConfigProvider<Languages> {
	@Autowired
	private LanguagesTemplate template;

	@Override
	protected Languages load() {
		return read("languages", template.supply(new Languages()));
	}

	@Override
	public Class<Languages> getObjectType() {
		return Languages.class;
	}
}

