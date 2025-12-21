package me.whereareiam.yui.common.translation.loader;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.semantica.locale.LocaleParser;
import me.whereareiam.semantica.model.translation.entry.LocalizedEntry;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.translation.TranslationService;
import me.whereareiam.semantica.translation.base.TranslationLocale;
import me.whereareiam.yui.Constants;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class PluginTranslationLoader extends AbstractTranslationLoader {
    private final TranslationService<DiscordLocale> translationService;
    private final Path pluginsPath;
    private final LocaleParser<DiscordLocale> localeParser;

	public PluginTranslationLoader(
            TranslationService<DiscordLocale> translationService,
            @Qualifier("pluginsPath") Path pluginsPath,
            LocaleParser<DiscordLocale> localeParser
    ) {
		this.translationService = translationService;
		this.pluginsPath = pluginsPath;
		this.localeParser = localeParser;
	}

	public void loadPlugin(String pluginId) {
        if (pluginId == null || pluginId.isBlank()) return;

        Path languagesDir = pluginsPath
                .resolve(pluginId)
                .resolve(Constants.Structure.languagesDir);

        if (!Files.isDirectory(languagesDir)) {
            log.debug("[PluginTranslations] No translations for plugin: {}", pluginId);
            return;
        }

        String prefix = "plugin." + pluginId.toLowerCase() + ".";
        
        // Accumulator to collect all locales for each key
        Map<String, Map<TranslationLocale, String>> accumulator = new HashMap<>();
        AtomicInteger templateCount = new AtomicInteger(0);

        // Load all translation files (LOCALE, MULTI_LOCALE, and TEMPLATE types)
        loadFromDirectory(
                languagesDir,
                // LOCALE processor
                (_, locale, translations) -> {
                    TranslationLocale translationLocale = localeParser.parse(locale);
                    
                    translations.forEach((key, textValue) -> {
                        String text = textValue.asString();
                        String fullKey = prefix + key;
                        accumulator.computeIfAbsent(fullKey, _ -> new HashMap<>()).put(translationLocale, text);
                    });
                },
                // MULTI_LOCALE processor
                (file, multiLocaleData) -> {
                    multiLocaleData.forEach((key, localeMap) -> {
                        String fullKey = prefix + key;
                        Map<TranslationLocale, String> translations = new HashMap<>();
                        
                        localeMap.forEach((locale, textValue) -> {
                            TranslationLocale translationLocale = localeParser.parse(locale);
                            translations.put(translationLocale, textValue.asString());
                        });
                        
                        accumulator.computeIfAbsent(fullKey, _ -> new HashMap<>()).putAll(translations);
                    });
                    
                    log.info("[PluginTranslations] Loaded {} multi-locale entries from '{}' for plugin: {}", 
                            multiLocaleData.size(), file.getFileName(), pluginId);
                },
                // TEMPLATE processor
                (file, templates) -> {
                    templates.forEach((key, textValue) -> {
                        String fullKey = prefix + key;
                        TemplateEntry entry = new TemplateEntry(textValue.asString());
                        translationService.register(fullKey, entry);
                        templateCount.incrementAndGet();
                    });
                    log.info("[PluginTranslations] Loaded {} templates from '{}' for plugin: {}", 
                            templates.size(), file.getFileName(), pluginId);
                }
        );

        // Register all accumulated localized entries
        accumulator.forEach((key, translations) -> {
            LocalizedEntry entry = new LocalizedEntry(translations);
            translationService.register(key, entry);
        });

        log.info("[PluginTranslations] Loaded {} localized keys and {} template entries for plugin: {}", 
                accumulator.size(), templateCount.get(), pluginId);
    }

    public void unloadPlugin(String pluginId) {
        if (pluginId == null || pluginId.isBlank()) return;

        String prefix = "plugin." + pluginId.toLowerCase() + ".";
        int removed = 0;

        for (String key : translationService.getKeys(prefix)) {
            translationService.unregister(key);
            removed++;
        }

        if (removed > 0) {
            log.info("[PluginTranslations] Unloaded {} keys for plugin: {}", removed, pluginId);
        }
    }
}
