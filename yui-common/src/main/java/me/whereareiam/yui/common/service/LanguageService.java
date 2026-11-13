package me.whereareiam.yui.common.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.Registry;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yui.common.config.provider.LanguagesProvider;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.persistence.LanguagePersistence;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LanguageService implements Reloadable {
	private final LanguagesProvider languagesProvider;
	private final LanguagePersistence languagePersistence;
	private final ObjectProvider<Settings> settings;
	private final Registry<Reloadable> reloadableRegistry;

	@PostConstruct
	private void onStartup() {
		reloadableRegistry.register(this);
		performSync();
	}

	@Override
	public void reload() {
		syncLanguagesWithDatabase();
	}

	/**
	 * Public method to trigger language sync (can be called on config reload).
	 */
	public void syncLanguagesWithDatabase() {
		performSync();
	}

	private void performSync() {
		try {
			Set<DiscordLocale> configLocales = new LinkedHashSet<>(languagesProvider.get().all().keySet());

			// The default locale is always offered, listed or not.
			DiscordLocale defaultLocale = settings.getObject().getLocale();
			if (defaultLocale != DiscordLocale.UNKNOWN) configLocales.add(defaultLocale);

			for (DiscordLocale locale : configLocales) {
				// Add to database if not exists
				if (!languagePersistence.languageExists(locale)) {
					languagePersistence.addLanguage(locale);
					log.info("[LanguageService] Added language {} to database", locale);
				}
			}

			// Remove languages from database that are not in config
			List<DiscordLocale> allDbLocales = languagePersistence.getAvailableLanguages();

			for (DiscordLocale dbLocale : allDbLocales) {
				if (!configLocales.contains(dbLocale)) {
					languagePersistence.removeLanguage(dbLocale);
					log.info("[LanguageService] Removed language {} from database", dbLocale);
				}
			}

			log.debug("[LanguageService] Finished syncing languages with database");
		} catch (Exception e) {
			log.error("[LanguageService] Error syncing languages with database", e);
		}
	}
}

