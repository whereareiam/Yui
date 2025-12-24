package me.whereareiam.yui.common.config.template;

import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.yui.model.config.languages.LanguageEntry;
import me.whereareiam.yui.model.config.languages.Languages;
import me.whereareiam.yui.model.config.languages.TranslationSettings;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class LanguagesTemplate implements TemplateProvider<Languages> {
	@Override
	public Languages supply(Languages config) {
		if (config.getLanguages() == null) {
			config.setLanguages(new HashMap<>());
		} else {
			// Remove any invalid locale strings (that would deserialize to UNKNOWN)
			config.getLanguages().keySet().removeIf(key -> {
				DiscordLocale locale = DiscordLocale.from(key);
				return locale == DiscordLocale.UNKNOWN;
			});
		}

		String defaultLocaleKey = DiscordLocale.ENGLISH_US.getLocale();
		if (!config.getLanguages().containsKey(defaultLocaleKey)) {
			LanguageEntry defaultLanguage = new LanguageEntry();
			defaultLanguage.setEnabled(true);
			defaultLanguage.setDisplayName("English (US)");
			config.getLanguages().put(defaultLocaleKey, defaultLanguage);
		}

		config.setSettings(new TranslationSettings());

		return config;
	}
}

