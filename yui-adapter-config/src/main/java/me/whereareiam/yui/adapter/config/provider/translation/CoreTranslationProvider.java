package me.whereareiam.yui.adapter.config.provider.translation;

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
public class CoreTranslationProvider extends AbstractTranslationLoader {
	private final Path dataPath;

	public CoreTranslationProvider(
			@Qualifier("dataPath") Path dataPath
	) {
		this.dataPath = dataPath;
	}

	@Override
	public Map<String, Map<DiscordLocale, Map<String, String>>> load() {
		Map<String, Map<DiscordLocale, Map<String, String>>> result = new HashMap<>();

		Path languagesDir = dataPath.resolve(Constants.Structure.languagesDir);
		if (!Files.isDirectory(languagesDir)) {
			log.warn("[TranslationService]: Core languages directory not found: {}", languagesDir);
			return result;
		}

		Map<DiscordLocale, Map<String, String>> localeMap = processLanguageFolder(languagesDir);
		log.debug("[TranslationService]: Loaded core translations for {} {}", localeMap.size(), localeMap.size() == 1 ? "locale" : "locales");
		result.put("", localeMap);

		return result;
	}
}