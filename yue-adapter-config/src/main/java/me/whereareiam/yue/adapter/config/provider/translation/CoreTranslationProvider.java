package me.whereareiam.yue.adapter.config.provider.translation;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.whereareiam.yue.shared.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class CoreTranslationProvider extends AbstractTranslationLoader {
	private static final Logger logger = LoggerFactory.getLogger(CoreTranslationProvider.class);
	private final Path dataPath;

	public CoreTranslationProvider(@Qualifier("dataPath") Path dataPath, ObjectMapper objectMapper) {
		super(objectMapper);
		this.dataPath = dataPath;
	}

	@Override
	public Map<String, Map<Locale, Map<String, String>>> loadAll() {
		Map<String, Map<Locale, Map<String, String>>> result = new HashMap<>();

		Path languagesDir = dataPath.resolve(Constants.Structure.languagesDir);
		if (!Files.isDirectory(languagesDir)) {
			logger.warn("Core languages directory not found: {}", languagesDir);
			return result;
		}

		Map<Locale, Map<String, String>> localeMap = processLanguageFolder(languagesDir);
		logger.info("Loaded core translations for {} {}", localeMap.size(), localeMap.size() == 1 ? "locale" : "locales");
		result.put("", localeMap);

		return result;
	}
}