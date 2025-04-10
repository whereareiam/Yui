package me.whereareiam.yue.adapter.config.provider.translation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import me.whereareiam.yue.api.input.translation.TranslationLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CoreTranslationProvider implements TranslationLoader {

	private final Path dataPath;
	private final ObjectMapper objectMapper;

	private final Map<Locale, Map<String, String>> coreTranslations = new ConcurrentHashMap<>();

	public CoreTranslationProvider(@Qualifier("dataPath") Path dataPath,
	                               ObjectMapper objectMapper) {
		this.dataPath = dataPath;
		this.objectMapper = objectMapper;
	}

	@PostConstruct
	public void init() {
		loadCoreTranslations();
	}

	private void loadCoreTranslations() {
		Path languagesPath = dataPath.resolve("languages");
		if (!Files.exists(languagesPath) || !Files.isDirectory(languagesPath)) {
			return;
		}

		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(languagesPath, "*.json")) {
			for (Path file : dirStream) {
				loadOneFile(file);
			}
		} catch (IOException e) {
			// log or handle
		}
	}

	@SuppressWarnings("unchecked")
	private void loadOneFile(Path file) {
		String filename = file.getFileName().toString(); // e.g. "en.json"
		String localeStr = filename.substring(0, filename.lastIndexOf('.'));
		Locale locale = Locale.forLanguageTag(localeStr);

		try {
			Map<String, String> content =
					objectMapper.readValue(Files.newInputStream(file), Map.class);

			coreTranslations
					.computeIfAbsent(locale, x -> new ConcurrentHashMap<>())
					.putAll(content);

		} catch (IOException e) {
			// log or handle
		}
	}

	@Override
	public Map<String, Map<Locale, Map<String, String>>> loadAll() {
		// We'll package our single "core" domain into the structure:
		// "core" -> (locale -> (key -> text))
		Map<String, Map<Locale, Map<String, String>>> result = new HashMap<>();
		result.put("core", coreTranslations);
		return result;
	}
}