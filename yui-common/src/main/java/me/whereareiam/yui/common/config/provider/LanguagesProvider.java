package me.whereareiam.yui.common.config.provider;

import me.whereareiam.yui.model.config.languages.Languages;
import org.springframework.stereotype.Component;

/**
 * Reads {@code languages.yml}. Without a file no language is listed, and Yui offers only its default locale.
 */
@Component
public class LanguagesProvider extends DefaultConfigProvider<Languages> {
	@Override
	protected Languages load() {
		return read("languages", new Languages());
	}

	@Override
	public Class<Languages> getObjectType() {
		return Languages.class;
	}
}
