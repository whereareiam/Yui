package me.whereareiam.yue.adapter.config.provider.translation;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class ModuleTranslationProvider extends AbstractTranslationLoader {
	private static final Logger logger = LoggerFactory.getLogger(ModuleTranslationProvider.class);
	private final Path modulesPath;

	public ModuleTranslationProvider(@Qualifier("modulesPath") Path modulesPath,
	                                 ObjectMapper objectMapper) {
		super(objectMapper);
		this.modulesPath = modulesPath;
	}

	@Override
	public Map<String, Map<Locale, Map<String, String>>> loadAll() {
		logger.info("Loading module translations from modules path: {}", modulesPath);
		Map<String, Map<Locale, Map<String, String>>> result = new HashMap<>();

		try (var modulesDirStream = Files.list(modulesPath)) {
			modulesDirStream
					.filter(Files::isDirectory)
					.forEach(moduleDir -> {
						String moduleName = moduleDir.getFileName().toString();
						String prefix = "module." + moduleName + ".";
						logger.debug("Processing module: {} with prefix: {}", moduleName, prefix);

						Path languageFolder = moduleDir.resolve("languages");
						if (!Files.isDirectory(languageFolder)) {
							logger.debug("No languages directory found for module: {}", moduleName);
							return;
						}

						Map<Locale, Map<String, String>> localeMap = processLanguageFolder(languageFolder);
						logger.info("Loaded translations for module {} with {} locales", moduleName, localeMap.size());
						result.put(prefix, localeMap);
					});
		} catch (Exception e) {
			logger.error("Error loading module translations", e);
		}

		return result;
	}
}