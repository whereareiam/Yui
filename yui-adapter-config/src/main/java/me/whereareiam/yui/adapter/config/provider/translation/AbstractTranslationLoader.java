package me.whereareiam.yui.adapter.config.provider.translation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.input.translation.TranslationLoader;
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
	protected final ObjectMapper objectMapper;

	protected AbstractTranslationLoader(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	protected Map<String, Object> loadFile(Path file) {
		try {
			log.debug("Loading translation file: {}", file);
			return objectMapper.readValue(file.toFile(), new TypeReference<>() {});
		} catch (Exception e) {
			log.error("Failed to load translation file: {}", file, e);
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
		log.debug("Processing language folder: {}", languageFolder);
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
							log.debug("Processing language file: {} for locale: {}", file, locale);
							Map<String, String> current = localeMap.computeIfAbsent(locale, l -> new HashMap<>());

							Map<String, Object> raw = loadFile(file);
							Map<String, String> flattened = new HashMap<>();
							flattenMap("", raw, flattened);

							current.putAll(flattened);
							log.debug("Added {} translations for locale {}", flattened.size(), locale);
						}
					});
		} catch (Exception e) {
			log.error("Error processing language folder: {}", languageFolder, e);
		}

		return localeMap;
	}
}