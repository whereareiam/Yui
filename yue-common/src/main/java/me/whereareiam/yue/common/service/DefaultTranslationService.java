package me.whereareiam.yue.common.service;

import lombok.AllArgsConstructor;
import me.whereareiam.yue.api.input.translation.TranslationLoader;
import me.whereareiam.yue.api.input.translation.TranslationService;
import me.whereareiam.yue.api.model.config.settings.Settings;
import me.whereareiam.yue.api.output.provider.Provider;
import me.whereareiam.yue.api.output.provider.UserProfileCacheProvider;
import me.whereareiam.yue.api.util.TranslationTags;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
public class DefaultTranslationService implements TranslationService {
	private static final Logger logger = LoggerFactory.getLogger(DefaultTranslationService.class);

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

	@Override
	public void initialize() {
		logger.debug("Initializing translation service");
		for (TranslationLoader loader : loaders) {
			logger.debug("Loading translations from: {}", loader.getClass().getSimpleName());
			Map<String, Map<DiscordLocale, Map<String, String>>> loaderResult = loader.loadAll();
			mergeLoaderResult(loaderResult);
		}

		logger.info("Translation service initialized with {} {}", translations.size(), translations.size() == 1 ? "locale" : "locales");
	}

	private void mergeLoaderResult(Map<String, Map<DiscordLocale, Map<String, String>>> loaderResult) {
		logger.debug("Merging translation loader results");
		for (Map.Entry<String, Map<DiscordLocale, Map<String, String>>> prefixEntry : loaderResult.entrySet()) {
			String prefix = prefixEntry.getKey();
			Map<DiscordLocale, Map<String, String>> localeMap = prefixEntry.getValue();
			logger.debug("Processing prefix: '{}'", prefix);

			for (Map.Entry<DiscordLocale, Map<String, String>> localeEntry : localeMap.entrySet()) {
				DiscordLocale locale = localeEntry.getKey();
				Map<String, String> translationsForThatLocale = localeEntry.getValue();
				logger.trace("Processing locale: {} with {} entries", locale, translationsForThatLocale.size());

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
		logger.trace("Translating key '{}' for user {}", key, userId);
		DiscordLocale[] userLocales = getUserLocalesOrDefault(userId, defaultBotLocale);
		logger.trace("User locales: {}", (Object) userLocales);

		for (DiscordLocale locale : userLocales) {
			String translation = getTranslatedString(locale, key);
			if (translation != null) {
				logger.trace("Found translation for key '{}' in locale {}: '{}'", key, locale, translation);
				return translation;
			}
		}

		logger.debug("No translation found for key: '{}'", key);
		return key;
	}

	@Override
	public String translate(String key, DiscordLocale locale) {
		logger.trace("Translating key '{}' for locale {}", key, locale);

		// First try with the specified locale
		String translation = getTranslatedString(locale, key);
		if (translation != null) {
			logger.trace("Found translation for key '{}' in specified locale {}: '{}'", key, locale, translation);
			return translation;
		}

		// Fallback to the default bot locale
		DiscordLocale defaultBotLocale = settings.get().getLocale();
		translation = getTranslatedString(defaultBotLocale, key);
		if (translation != null) {
			logger.trace("Found translation for key '{}' in default locale {}: '{}'", key, defaultBotLocale, translation);
			return translation;
		}

		logger.debug("No translation found for key: '{}'", key);
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
			logger.warn(
					"Failed to format translation '{}' with args {} for locale {} – returning unformatted",
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
					logger.trace("User {} locales: {}", userId, locales);
					return locales.toArray(DiscordLocale[]::new);
				})
				.orElseGet(() -> {
					logger.trace("No profile found for user {}, using default locale", userId);
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