package me.whereareiam.yui.common.localization.format;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.configura.Config;
import me.whereareiam.semantica.model.TextValue;
import me.whereareiam.yui.localization.format.FileFormat;
import me.whereareiam.yui.localization.format.FileFormats;
import me.whereareiam.yui.localization.loader.FileTypeHandler;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler for MULTI_LOCALE format files.
 * Processes files where keys contain maps of locales to translations.
 */
@Slf4j
public class MultiLocaleFileHandler implements FileTypeHandler {
    @Override
    public boolean canHandle(Path file, FileFormat format) {
        if (!format.getName().equals(FileFormats.MULTI_LOCALE.getName()))
            return false;
        
        // Load and check structure
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> content = Config.load(file, Map.class);

            return isMultiLocaleStructure(content);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void load(
            Path file,
            LocaleFileProcessor localeProcessor,
            MultiLocaleFileProcessor multiLocaleProcessor,
            TemplateFileProcessor templateProcessor
    ) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> content = Config.load(file, Map.class);
            
            // Extract filename without extension to use as prefix
            String filename = file.getFileName().toString();
            String prefix = filename.replaceFirst("\\.[^.]+$", "");
            
            Map<String, Map<DiscordLocale, TextValue>> result = new HashMap<>();
            flattenMultiLocaleMap(prefix, content, result);
            
            multiLocaleProcessor.process(file, result);
        } catch (Exception e) {
            log.error("[MultiLocaleFileHandler] Failed to load file: {}", file, e);
        }
    }

    /**
     * Check if the loaded content has a multi-locale structure.
     */
    @SuppressWarnings("unchecked")
    private boolean isMultiLocaleStructure(Map<String, Object> content) {
        if (content.isEmpty())
            return false;

        int localeMapCount = 0;
        int totalChecked = 0;

        for (Map.Entry<String, Object> entry : content.entrySet()) {
            if (totalChecked >= 5) break;
            totalChecked++;

            if (entry.getValue() instanceof Map) {
                Map<String, Object> valueMap = (Map<String, Object>) entry.getValue();
                if (hasLocaleMapStructure(valueMap))
                    localeMapCount++;
            }
        }

        return localeMapCount > 0;
    }

    /**
     * Check if a map has locale map structure (directly or in nested values).
     */
    @SuppressWarnings("unchecked")
    private boolean hasLocaleMapStructure(Map<String, Object> map) {
        if (containsLocaleKeys(map))
            return true;

        for (Object value : map.values()) {
            if (value instanceof Map) {
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                if (containsLocaleKeys(nestedMap))
                    return true;
            }
        }

        return false;
    }

    /**
     * Check if a map contains keys that look like locale identifiers.
     */
    private boolean containsLocaleKeys(Map<String, Object> map) {
        for (String key : map.keySet()) {
            try {
                DiscordLocale locale = DiscordLocale.from(key.replace("_", "-"));
                if (locale != DiscordLocale.UNKNOWN) return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    /**
     * Recursively flatten multi-locale structure with dot notation.
     */
    @SuppressWarnings("unchecked")
    private void flattenMultiLocaleMap(
            String prefix,
            Map<String, Object> map,
            Map<String, Map<DiscordLocale, TextValue>> result
    ) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            Object value = entry.getValue();

            if (value instanceof Map) {
                Map<String, Object> valueMap = (Map<String, Object>) value;

                if (containsLocaleKeys(valueMap)) {
                    Map<DiscordLocale, TextValue> localeTranslations = new HashMap<>();

                    for (Map.Entry<String, Object> localeEntry : valueMap.entrySet()) {
                        try {
                            DiscordLocale locale = DiscordLocale.from(localeEntry.getKey().replace("_", "-"));
                            if (locale != DiscordLocale.UNKNOWN)
                                localeTranslations.put(locale, TextValue.from(localeEntry.getValue()));
                        } catch (Exception e) {
                            log.warn("[MultiLocaleFileHandler] Invalid locale key '{}' in file", localeEntry.getKey());
                        }
                    }

                    if (!localeTranslations.isEmpty())
                        result.put(fullKey, localeTranslations);

                    continue;
                }

                flattenMultiLocaleMap(fullKey, valueMap, result);
            }
        }
    }
}
