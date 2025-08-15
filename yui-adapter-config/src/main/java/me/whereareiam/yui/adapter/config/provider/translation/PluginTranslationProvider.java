package me.whereareiam.yui.adapter.config.provider.translation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.shared.Constants;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PluginTranslationProvider extends AbstractTranslationLoader {
	private final Path pluginsPath;

	public PluginTranslationProvider(@Qualifier("pluginsPath") Path pluginsPath,
	                                 ObjectMapper objectMapper) {
		super(objectMapper);
		this.pluginsPath = pluginsPath;
	}

	@Override
	public Map<String, Map<DiscordLocale, Map<String, String>>> loadAll() {
		Map<String, Map<DiscordLocale, Map<String, String>>> result = new HashMap<>();

		try (var pluginsDirStream = Files.list(pluginsPath)) {
			pluginsDirStream
					.filter(Files::isDirectory)
					.forEach(pluginDir -> {
						String pluginName = pluginDir.getFileName().toString().toLowerCase();
						String prefix = "plugin." + pluginName + ".";
						log.debug("[TranslationService]: Processing plugin: {} with prefix: {}", pluginName, prefix);

						Path languageFolder = pluginDir.resolve(Constants.Structure.languagesDir);
						if (!Files.isDirectory(languageFolder)) {
							log.debug("[TranslationService]: No languages directory found for plugin: {}", pluginName);
							return;
						}

						Map<DiscordLocale, Map<String, String>> localeMap = processLanguageFolder(languageFolder);
						log.debug("[TranslationService]: Loaded translations for plugin {} with {} locales", pluginName, localeMap.size());
						result.put(prefix, localeMap);
					});
		} catch (Exception e) {
			log.error("[TranslationService]: Error loading plugin translations", e);
		}

		return result;
	}
}