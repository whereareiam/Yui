package me.whereareiam.yue.adapter.config.provider.translation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.whereareiam.yue.api.input.translation.TranslationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class AbstractTranslationLoader implements TranslationLoader {
	private static final Logger logger = LoggerFactory.getLogger(AbstractTranslationLoader.class);
	protected final ObjectMapper objectMapper;

	protected AbstractTranslationLoader(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	protected Map<String, Object> loadFile(Path file) {
		try {
			logger.debug("Loading translation file: {}", file);
			return objectMapper.readValue(file.toFile(), new TypeReference<>() {});
		} catch (Exception e) {
			logger.error("Failed to load translation file: {}", file, e);
			return Collections.emptyMap();
		}
	}

	protected void flattenMap(String parentKey, Map<String, Object> raw, Map<String, String> flat) {
		for (Map.Entry<String, Object> entry : raw.entrySet()) {
			String key = parentKey.isEmpty()
					? entry.getKey()
					: (parentKey + "." + entry.getKey());

			if (entry.getValue() instanceof Map) {
				flattenMap(key, (Map<String, Object>) entry.getValue(), flat);
			} else {
				flat.put(key, String.valueOf(entry.getValue()));
			}
		}
	}

	protected Map<Locale, Map<String, String>> processLanguageFolder(Path languageFolder) {
		logger.info("Processing language folder: {}", languageFolder);
		Map<Locale, Map<String, String>> localeMap = new HashMap<>();

		try (var langFiles = Files.list(languageFolder)) {
			langFiles
					.filter(Files::isRegularFile)
					.forEach(file -> {
						String filename = file.getFileName().toString();
						if (filename.contains(".")) {
							String[] parts = filename.split("\\.");
							String languageCode = parts[0];
							String extension = parts[1];

							Locale locale = Locale.forLanguageTag(languageCode);
							logger.debug("Processing language file: {} for locale: {}", file, locale);
							Map<String, String> current = localeMap.computeIfAbsent(locale, l -> new HashMap<>());

							Map<String, Object> raw = loadFile(file);
							Map<String, String> flattened = new HashMap<>();
							flattenMap("", raw, flattened);

							current.putAll(flattened);
							logger.debug("Added {} translations for locale {}", flattened.size(), locale);
						}
					});
		} catch (Exception e) {
			logger.error("Error processing language folder: {}", languageFolder, e);
		}

		return localeMap;
	}
}