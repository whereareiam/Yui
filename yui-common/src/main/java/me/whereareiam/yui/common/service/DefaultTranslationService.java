package me.whereareiam.yui.common.service;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.input.Registry;
import me.whereareiam.yui.api.input.translation.TranslationLoader;
import me.whereareiam.yui.api.input.translation.TranslationService;
import me.whereareiam.yui.api.model.config.settings.Settings;
import me.whereareiam.yui.api.output.Reloadable;
import me.whereareiam.yui.api.output.provider.Provider;
import me.whereareiam.yui.api.output.provider.UserProfileCacheProvider;
import me.whereareiam.yui.api.util.TranslationTags;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DefaultTranslationService implements TranslationService, Reloadable {
	private final Provider<Settings> settings;
	private final List<TranslationLoader> loaders;
	private final UserProfileCacheProvider userProfileCache;

	/**
	 * Merged translations:
	 * Map<DiscordLocale, Map<String, String>>
	 * ^            ^
	 * e.g. "en-US" -> { "vocabulary.cancel" -> "Cancel",
	 * "plugin.music.vocabulary.cancel" -> "Stop",
	 * ... }
	 */
	private final Map<DiscordLocale, Map<String, String>> translations = new ConcurrentHashMap<>();

	@Autowired
	public DefaultTranslationService(
			Provider<Settings> settings,
			List<TranslationLoader> loaders,
			UserProfileCacheProvider userProfileCache,
			Registry<Reloadable> reloadableRegistry
	) {
		this.settings = settings;
		this.loaders = loaders;
		this.userProfileCache = userProfileCache;

		reloadableRegistry.register(this);
	}

	public void initialize() {
		log.debug("[TranslationService]: Initializing translation service");
		for (TranslationLoader loader : loaders) {
			log.debug("[TranslationService]: Loading translations from: {}", loader.getClass().getSimpleName());
			Map<String, Map<DiscordLocale, Map<String, String>>> loaderResult = loader.loadAll();
			mergeLoaderResult(loaderResult);
		}

		log.info(
				"[TranslationService]: Translations initialized with {} {} ({} keys)",
				translations.size(),
				translations.size() == 1 ? "locale" : "locales",
				translations.values().stream().mapToInt(Map::size).sum()
		);
	}

	@Override
	public void reload() {
		log.debug("[TranslationService]: Reloading translation service");
		translations.clear();
		initialize();
		log.debug("[TranslationService]: Translation service reloaded successfully");
	}

	private void mergeLoaderResult(Map<String, Map<DiscordLocale, Map<String, String>>> loaderResult) {
		log.debug("[TranslationService]: Merging translation loader results");
		for (Map.Entry<String, Map<DiscordLocale, Map<String, String>>> prefixEntry : loaderResult.entrySet()) {
			String prefix = prefixEntry.getKey();
			Map<DiscordLocale, Map<String, String>> localeMap = prefixEntry.getValue();
			log.debug("[TranslationService]: Processing prefix: '{}'", prefix);

			for (Map.Entry<DiscordLocale, Map<String, String>> localeEntry : localeMap.entrySet()) {
				DiscordLocale locale = localeEntry.getKey();
				Map<String, String> translationsForThatLocale = localeEntry.getValue();
				log.trace("[TranslationService]: Processing locale: {} with {} entries", locale, translationsForThatLocale.size());

				Map<String, String> targetMap = translations.computeIfAbsent(locale, _ -> new ConcurrentHashMap<>());

				for (Map.Entry<String, String> kv : translationsForThatLocale.entrySet()) {
					String finalKey = prefix.isEmpty()
							? kv.getKey()
							: prefix + kv.getKey();
					targetMap.put(finalKey, kv.getValue());
				}
			}
		}
	}

	@Override
	public String translate(String key, long userId) {
		DiscordLocale defaultBotLocale = settings.get().getLocale();
		log.trace("[TranslationService]: Translating key '{}' for user {}", key, userId);
		DiscordLocale[] userLocales = getUserLocalesOrDefault(userId, defaultBotLocale);
		log.trace("[TranslationService]: User locales: {}", (Object) userLocales);

		for (DiscordLocale locale : userLocales) {
			String translation = getTranslatedString(locale, key);
			if (translation != null) {
				log.trace("[TranslationService]: Found translation for key '{}' in locale {}: '{}'", key, locale, translation);
				return translation;
			}
		}

		log.debug("[TranslationService]: No translation found for key: '{}'", key);
		return key;
	}

	@Override
	public String translate(String key, DiscordLocale locale) {
		log.trace("[TranslationService]: Translating key '{}' for locale {}", key, locale);

		// First try with the specified locale
		String translation = getTranslatedString(locale, key);
		if (translation != null) {
			log.trace("[TranslationService]: Found translation for key '{}' in specified locale {}: '{}'", key, locale, translation);
			return translation;
		}

		// Fallback to the default bot locale
		DiscordLocale defaultBotLocale = settings.get().getLocale();
		translation = getTranslatedString(defaultBotLocale, key);
		if (translation != null) {
			log.trace("[TranslationService]: Found translation for key '{}' in default locale {}: '{}'", key, defaultBotLocale, translation);
			return translation;
		}

		log.debug("[TranslationService]: No translation found for key: '{}'", key);
		return key;
	}

	@Override
	public String translate(String key, long userId, Object... args) {
		String pattern = translate(key, userId);
		DiscordLocale locale = getEffectiveLocaleForUser(userId);
		return format(pattern, locale, args);
	}

	@Override
	public String translate(String key, DiscordLocale locale, Object... args) {
		String pattern = translate(key, locale);
		return format(pattern, locale, args);
	}

	private DiscordLocale getEffectiveLocaleForUser(long userId) {
		DiscordLocale defaultLocale = settings.get().getLocale();
		return userProfileCache.getProfile(userId)
				.map(profile -> profile.getPrimaryLanguage() != null ? profile.getPrimaryLanguage() : defaultLocale)
				.orElse(defaultLocale);
	}

	private String format(String pattern, DiscordLocale locale, Object... args) {
		if (pattern == null || pattern.isEmpty())
			return pattern;

		if (args == null || args.length == 0)
			return TranslationTags.resolve(pattern, locale);

		try {
			MessageFormat messageFormat = new MessageFormat(pattern);
			String formatted = messageFormat.format(args);

			return TranslationTags.resolve(formatted, locale);
		} catch (IllegalArgumentException ex) {
			log.warn(
					"[TranslationService]: Failed to format translation '{}' with args {} for locale {} – returning unformatted",
					pattern, Arrays.toString(args), locale, ex
			);
			return TranslationTags.resolve(pattern, locale);
		}
	}

	private DiscordLocale[] getUserLocalesOrDefault(long userId, DiscordLocale defaultBotLocale) {
		return userProfileCache.getProfile(userId)
				.map(profile -> {
					List<DiscordLocale> locales = new ArrayList<>();
					if (profile.getPrimaryLanguage() != null)
						locales.add(profile.getPrimaryLanguage());

					if (profile.getAdditionalLanguages() != null)
						locales.addAll(Arrays.asList(profile.getAdditionalLanguages()));

					locales.add(defaultBotLocale);
					log.trace("[TranslationService]: User {} locales: {}", userId, locales);
					return locales.toArray(DiscordLocale[]::new);
				})
				.orElseGet(() -> {
					log.trace("[TranslationService]: No profile found for user {}, using default locale", userId);
					return new DiscordLocale[]{defaultBotLocale};
				});
	}

	private String getTranslatedString(DiscordLocale locale, String key) {
		Map<String, String> translationsForLocale = translations.get(locale);
		if (translationsForLocale != null)
			return translationsForLocale.get(key);

		return null;
	}
}