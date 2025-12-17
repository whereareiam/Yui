package me.whereareiam.yui.common.translation;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.configura.Config;
import me.whereareiam.yui.translation.TranslationLoader;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractTranslationLoader implements TranslationLoader {
	@SuppressWarnings("unchecked")
	protected Map<String, Object> load(Path file) {
		try {
			log.debug("[TranslationService]: Loading translation file: {}", file);
			return Config.load(file, Map.class);
		} catch (Exception e) {
			log.error("[TranslationService]: Failed to load translation file: {}", file, e);
			return Collections.emptyMap();
		}
	}

	@SuppressWarnings("unchecked")
	protected void flattenMap(String parentKey, Map<String, Object> raw, Map<String, String> flat) {
		for (Map.Entry<String, Object> entry : raw.entrySet()) {
			String key = parentKey.isEmpty()
					? entry.getKey()
					: (parentKey + "." + entry.getKey());

			Object value = entry.getValue();
			if (value instanceof Map) {
				flattenMap(key, (Map<String, Object>) value, flat);
			} else if (value instanceof List<?> list) {
				String joined = list.stream()
						.map(String::valueOf)
						.collect(Collectors.joining("\n"));
				flat.put(key, joined);
			} else {
				flat.put(key, String.valueOf(value));
			}
		}
	}

	protected Map<DiscordLocale, Map<String, String>> processLanguageFolder(Path languageFolder) {
		log.debug("[TranslationService]: Processing language folder: {}", languageFolder);
		Map<DiscordLocale, Map<String, String>> localeMap = new HashMap<>();

		try (var langFiles = Files.list(languageFolder)) {
			langFiles
					.filter(Files::isRegularFile)
					.forEach(file -> {
						String filename = file.getFileName().toString();
						if (filename.contains(".")) {
							String[] parts = filename.split("\\.");
							String languageCode = parts[0];

							DiscordLocale locale = DiscordLocale.from(languageCode);
							log.debug("[TranslationService]: Processing language file: {} for locale: {}", file, locale);
							Map<String, String> current = localeMap.computeIfAbsent(locale, _ -> new HashMap<>());

							Map<String, Object> raw = load(file);
							Map<String, String> flattened = new HashMap<>();
							flattenMap("", raw, flattened);

							current.putAll(flattened);
							log.debug("[TranslationService]: Added {} translations for locale {}", flattened.size(), locale);
						}
					});
		} catch (Exception e) {
			log.error("[TranslationService]: Error processing language folder: {}", languageFolder, e);
		}

		return localeMap;
	}
}