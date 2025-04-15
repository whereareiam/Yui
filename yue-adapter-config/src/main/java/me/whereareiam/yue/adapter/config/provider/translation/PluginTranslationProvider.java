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
public class PluginTranslationProvider extends AbstractTranslationLoader {
	private static final Logger logger = LoggerFactory.getLogger(PluginTranslationProvider.class);
	private final Path pluginsPath;

	public PluginTranslationProvider(@Qualifier("pluginsPath") Path pluginsPath,
	                                 ObjectMapper objectMapper) {
		super(objectMapper);
		this.pluginsPath = pluginsPath;
	}

	@Override
	public Map<String, Map<Locale, Map<String, String>>> loadAll() {
		Map<String, Map<Locale, Map<String, String>>> result = new HashMap<>();

		try (var pluginsDirStream = Files.list(pluginsPath)) {
			pluginsDirStream
					.filter(Files::isDirectory)
					.forEach(pluginDir -> {
						String pluginName = pluginDir.getFileName().toString();
						String prefix = "plugin." + pluginName + ".";
						logger.debug("Processing plugin: {} with prefix: {}", pluginName, prefix);

						Path languageFolder = pluginDir.resolve(Constants.Structure.languagesDir);
						if (!Files.isDirectory(languageFolder)) {
							logger.debug("No languages directory found for plugin: {}", pluginName);
							return;
						}

						Map<Locale, Map<String, String>> localeMap = processLanguageFolder(languageFolder);
						logger.info("Loaded translations for plugin {} with {} locales", pluginName, localeMap.size());
						result.put(prefix, localeMap);
					});
		} catch (Exception e) {
			logger.error("Error loading plugin translations", e);
		}

		return result;
	}
}