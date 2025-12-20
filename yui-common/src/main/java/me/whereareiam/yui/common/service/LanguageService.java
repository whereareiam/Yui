package me.whereareiam.yui.common.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.Registry;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yui.common.config.provider.LanguagesProvider;
import me.whereareiam.yui.service.RoleService;
import me.whereareiam.yui.model.config.languages.LanguageEntry;
import me.whereareiam.yui.model.config.languages.Languages;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.persistence.LanguagePersistence;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LanguageService implements Reloadable {
	private final LanguagesProvider languagesProvider;
	private final LanguagePersistence languagePersistence;
	private final RoleService roleService;
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
			Languages config = languagesProvider.get();
			if (config == null || config.getLanguages() == null) {
				log.warn("Languages config is null or empty, skipping sync");
				return;
			}

			Settings settingsObj = settings.getObject();
			DiscordLocale defaultLocale = settingsObj.getLocale();
			String defaultLocaleKey = defaultLocale.getLocale();

			// Ensure default language is included in config (skip if UNKNOWN)
			if (defaultLocale != DiscordLocale.UNKNOWN && !config.getLanguages().containsKey(defaultLocaleKey)) {
				LanguageEntry defaultLanguage = new LanguageEntry();
				defaultLanguage.setEnabled(true);
				defaultLanguage.setDisplayName(defaultLocale.getNativeName());
				config.getLanguages().put(defaultLocaleKey, defaultLanguage);
				log.info("Added default language {} to languages config", defaultLocale);
			}

			Set<DiscordLocale> configLocales = new HashSet<>();

			// Add/update enabled languages from config
			Map<DiscordLocale, LanguageEntry> languagesMap = config.toLocaleMap();
			for (var entry : languagesMap.entrySet()) {
				DiscordLocale locale = entry.getKey();
				LanguageEntry languageEntry = entry.getValue();

				if (!languageEntry.isEnabled()) {
					log.debug("Skipping disabled language: {}", locale);
					continue;
				}

				// Validate role if specified
				if (languageEntry.getRole() != null) {
					if (!roleService.isRoleAllowed(languageEntry.getRole())) {
						log.warn("Language {} has role {} which is not in allowed roles. Skipping language.", locale, languageEntry.getRole());
						continue;
					}
				}

				// Add to database if not exists
				if (!languagePersistence.languageExists(locale)) {
					languagePersistence.addLanguage(locale);
					log.info("Added language {} to database", locale);
				}

				configLocales.add(locale);
			}

			// Remove languages from database that are not in config
			List<DiscordLocale> allDbLocales = languagePersistence.getAvailableLanguages();

			for (DiscordLocale dbLocale : allDbLocales) {
				if (!configLocales.contains(dbLocale)) {
					languagePersistence.removeLanguage(dbLocale);
					log.info("Removed language {} from database (not in config)", dbLocale);
				}
			}

			log.info("Finished syncing languages with database");
		} catch (Exception e) {
			log.error("Error syncing languages with database", e);
		}
	}
}

