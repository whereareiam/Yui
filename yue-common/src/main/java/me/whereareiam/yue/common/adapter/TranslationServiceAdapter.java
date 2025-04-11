package me.whereareiam.yue.common.adapter;

import jakarta.annotation.PostConstruct;
import me.whereareiam.yue.api.input.translation.TranslationLoader;
import me.whereareiam.yue.api.input.translation.TranslationService;
import me.whereareiam.yue.api.model.config.settings.Settings;
import me.whereareiam.yue.api.output.provider.UserProfileCacheProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TranslationServiceAdapter implements TranslationService {
	private static final Logger logger = LoggerFactory.getLogger(TranslationServiceAdapter.class);
	private final List<TranslationLoader> loaders;
	private final UserProfileCacheProvider userProfileCache;
	private final Settings settings;

	/**
	 * Merged translations:
	 * Map<Locale, Map<String, String>>
	 * ^            ^
	 * e.g. "en" -> { "vocabulary.cancel" -> "Cancel",
	 * "module.music.vocabulary.cancel" -> "Stop",
	 * ... }
	 */
	private final Map<Locale, Map<String, String>> translations = new ConcurrentHashMap<>();

	public TranslationServiceAdapter(
			List<TranslationLoader> loaders,
			UserProfileCacheProvider userProfileCache,
			Settings settings
	) {
		this.loaders = loaders;
		this.userProfileCache = userProfileCache;
		this.settings = settings;
	}

	@PostConstruct
	public void init() {
		logger.debug("Initializing translation service");
		for (TranslationLoader loader : loaders) {
			logger.debug("Loading translations from: {}", loader.getClass().getSimpleName());
			Map<String, Map<Locale, Map<String, String>>> loaderResult = loader.loadAll();
			mergeLoaderResult(loaderResult);
		}

		logger.info("Translation service initialized with {} {}", translations.size(), translations.size() == 1 ? "locale" : "locales");
	}

	private void mergeLoaderResult(Map<String, Map<Locale, Map<String, String>>> loaderResult) {
		logger.debug("Merging translation loader results");
		for (Map.Entry<String, Map<Locale, Map<String, String>>> prefixEntry : loaderResult.entrySet()) {
			String prefix = prefixEntry.getKey();
			Map<Locale, Map<String, String>> localeMap = prefixEntry.getValue();
			logger.debug("Processing prefix: '{}'", prefix);

			for (Map.Entry<Locale, Map<String, String>> localeEntry : localeMap.entrySet()) {
				Locale locale = localeEntry.getKey();
				Map<String, String> translationsForThatLocale = localeEntry.getValue();
				logger.trace("Processing locale: {} with {} entries", locale, translationsForThatLocale.size());

				Map<String, String> targetMap = translations.computeIfAbsent(locale, l -> new ConcurrentHashMap<>());

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
		Locale defaultBotLocale = settings.getLocale();
		logger.trace("Translating key '{}' for user {}", key, userId);
		Locale[] userLocales = getUserLocalesOrDefault(userId, defaultBotLocale);
		logger.trace("User locales: {}", (Object) userLocales);

		for (Locale locale : userLocales) {
			String translation = getTranslatedString(locale, key);
			if (translation != null) {
				logger.trace("Found translation for key '{}' in locale {}: '{}'", key, locale, translation);
				return translation;
			}
		}

		logger.debug("No translation found for key: '{}'", key);
		return key;
	}

	private Locale[] getUserLocalesOrDefault(long userId, Locale defaultBotLocale) {
		return userProfileCache.getProfile(userId)
				.map(profile -> {
					List<Locale> locales = new java.util.ArrayList<>();
					if (profile.getPrimaryLanguage() != null)
						locales.add(profile.getPrimaryLanguage());

					if (profile.getAdditionalLanguages() != null)
						locales.addAll(Arrays.asList(profile.getAdditionalLanguages()));

					locales.add(defaultBotLocale);
					logger.trace("User {} locales: {}", userId, locales);
					return locales.toArray(Locale[]::new);
				})
				.orElseGet(() -> {
					logger.trace("No profile found for user {}, using default locale", userId);
					return new Locale[]{defaultBotLocale};
				});
	}

	private String getTranslatedString(Locale locale, String key) {
		Map<String, String> translationsForLocale = translations.get(locale);
		if (translationsForLocale != null)
			return translationsForLocale.get(key);

		return null;
	}
}