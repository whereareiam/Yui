package me.whereareiam.yue.common.adapter;

import jakarta.annotation.PostConstruct;
import me.whereareiam.yue.api.input.translation.TranslationLoader;
import me.whereareiam.yue.api.input.translation.TranslationService;
import me.whereareiam.yue.api.model.config.settings.Settings;
import me.whereareiam.yue.api.model.profile.UserProfile;
import me.whereareiam.yue.api.output.provider.UserProfileCacheProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TranslationServiceAdapter implements TranslationService {
	private final List<TranslationLoader> loaders;
	private final UserProfileCacheProvider userProfileCache;
	private final Settings settings;

	private final Map<String, Map<Locale, Map<String, String>>> translations = new ConcurrentHashMap<>();

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
		// Aggregate all loader data
		for (TranslationLoader loader : loaders) {
			Map<String, Map<Locale, Map<String, String>>> map = loader.loadAll();
			mergeAll(map);
		}
	}

	private void mergeAll(Map<String, Map<Locale, Map<String, String>>> newData) {
		// For each domain
		for (Map.Entry<String, Map<Locale, Map<String, String>>> e : newData.entrySet()) {
			String domain = e.getKey();
			Map<Locale, Map<String, String>> localeMap = e.getValue();

			// domain -> ...
			translations.computeIfAbsent(domain, x -> new ConcurrentHashMap<>());

			// Merge locale submaps
			for (Map.Entry<Locale, Map<String, String>> locEntry : localeMap.entrySet()) {
				Locale loc = locEntry.getKey();
				Map<String, String> newStrings = locEntry.getValue();

				Map<String, String> existingStrings =
						translations.get(domain).computeIfAbsent(loc, x -> new ConcurrentHashMap<>());

				existingStrings.putAll(newStrings);  // merge
			}
		}
	}

	@Override
	public String translate(String key, long userId) {
		final String prefix = "module.";
		if (key.startsWith(prefix)) {
			// Handle module.<moduleName>.<subKey> format
			String remainder = key.substring(prefix.length()); // "music.play_button"
			int dotIdx = remainder.indexOf('.');
			if (dotIdx < 0) {
				// no further dot => not well-formed
				return fallbackNoSubkey(remainder, userId);
			}
			String domain = remainder.substring(0, dotIdx);   // e.g. "music"
			String subKey = remainder.substring(dotIdx + 1); // e.g. "play_button"
			return doTranslate(domain, subKey, userId);
		}

		// Also handle direct <domain>.<subKey> format
		int dotIdx = key.indexOf('.');
		if (dotIdx > 0) {
			String domain = key.substring(0, dotIdx);
			String subKey = key.substring(dotIdx + 1);
			return doTranslate(domain, subKey, userId);
		} else {
			// No dot, treat as subKey in core domain
			return doTranslate("core", key, userId);
		}
	}

	private String doTranslate(String domain, String subKey, long userId) {
		Optional<UserProfile> profileOpt = userProfileCache.getProfile(userId);

		// 1) user’s primary
		if (profileOpt.isPresent() && profileOpt.get().getPrimaryLanguage() != null) {
			Locale primary = profileOpt.get().getPrimaryLanguage();
			String text = fetch(domain, primary, subKey);
			if (text != null) return text;
		}

		// 2) user’s additional
		if (profileOpt.isPresent() && profileOpt.get().getAdditionalLanguages() != null) {
			for (Locale additional : profileOpt.get().getAdditionalLanguages()) {
				String text = fetch(domain, additional, subKey);
				if (text != null) return text;
			}
		}

		// 3) bot default
		String text = fetch(domain, settings.getLocale(), subKey);
		if (text != null) return text;

		// 4) fallback => subKey
		return subKey;
	}

	private String fetch(String domain, Locale locale, String subKey) {
		Map<Locale, Map<String, String>> domainMap = translations.get(domain);
		if (domainMap == null) return null;

		Map<String, String> translations = domainMap.get(locale);
		if (translations == null) return null;

		return translations.get(subKey);
	}

	private String fallbackNoSubkey(String domain, long userId) {
		return domain;
	}
}