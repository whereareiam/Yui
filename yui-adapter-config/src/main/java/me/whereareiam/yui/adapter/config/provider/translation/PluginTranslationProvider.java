package me.whereareiam.yui.adapter.config.provider.translation;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.shared.Constants;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Slf4j
@Component
public class PluginTranslationProvider extends AbstractTranslationLoader {
	private final Path pluginsPath;

	public PluginTranslationProvider(
			@Qualifier("pluginsPath") Path pluginsPath
	) {
		this.pluginsPath = pluginsPath;
	}

	@Override
	public Map<String, Map<DiscordLocale, Map<String, String>>> load() {
		// Defer plugin translations to event-driven loading via loadForPlugin(id)
		return Map.of();
	}

	@Override
	public Map<String, Map<DiscordLocale, Map<String, String>>> load(String pluginId) {
		if (pluginId == null || pluginId.isBlank()) return Map.of();

		String prefix = "plugin." + pluginId.toLowerCase() + ".";
		Path pluginDir = pluginsPath.resolve(pluginId);
		Path languageFolder = pluginDir.resolve(Constants.Structure.languagesDir);

		if (!Files.isDirectory(languageFolder))
			return Map.of();

		Map<DiscordLocale, Map<String, String>> localeMap = processLanguageFolder(languageFolder);
		return Map.of(prefix, localeMap);
	}
}