package me.whereareiam.yui.common.translation.loader.type;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.configura.Config;
import me.whereareiam.semantica.model.TextValue;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler for MULTI_LOCALE type files.
 * Processes files where keys contain maps of locales to translations.
 * The filename (without extension) is used as the key prefix.
 * Example structure:
 * <pre>
 * # vocabulary.yml
 * cancel:
 *   en-US: "Cancel"
 *   de: "Abbrechen"
 * 
 * Results in keys: vocabulary.cancel
 * </pre>
 */
@Slf4j
public class MultiLocaleFileType {
    /**
     * Load a multi-locale file where keys contain maps of locales to translations.
     * The filename (without extension) is used as the key prefix.
     *
     * @param file the file to load
     * @return map of keys to locale-translation maps
     */
    public Map<String, Map<DiscordLocale, TextValue>> load(Path file) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> content = Config.load(file, Map.class);
            Map<String, Map<DiscordLocale, TextValue>> result = new HashMap<>();
            
            // Extract filename without extension to use as prefix
            String filename = file.getFileName().toString();
            String prefix = filename.replaceFirst("\\.[^.]+$", "");
            
            flattenMultiLocaleMap(prefix, content, result);
            
            log.debug("[MultiLocaleFileType] Loaded {} keys from '{}'", result.size(), filename);
            
            return result;
        } catch (Exception e) {
            log.error("[MultiLocaleFileType] Failed to load file: {}", file, e);
            return new HashMap<>();
        }
    }

    /**
     * Recursively flatten multi-locale structure with dot notation.
     *
     * @param prefix current key prefix
     * @param map map to flatten
     * @param result accumulator for flattened results
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

                // Check if this map contains locale keys
                if (containsLocaleKeys(valueMap)) {
                    // This is a locale map - extract locale translations
                    Map<DiscordLocale, TextValue> localeTranslations = new HashMap<>();

                    for (Map.Entry<String, Object> localeEntry : valueMap.entrySet()) {
                        try {
                            DiscordLocale locale = DiscordLocale.from(localeEntry.getKey().replace("_", "-"));
                            // Skip UNKNOWN locale
                            if (locale != DiscordLocale.UNKNOWN) {
                                localeTranslations.put(locale, TextValue.from(localeEntry.getValue()));
                            }
                        } catch (Exception e) {
                            log.warn("[MultiLocaleFileType] Invalid locale key '{}' in file", localeEntry.getKey());
                        }
                    }

                    if (!localeTranslations.isEmpty()) {
                        result.put(fullKey, localeTranslations);
                    }
                } else {
                    // This is a nested structure - recurse
                    flattenMultiLocaleMap(fullKey, valueMap, result);
                }
            }
        }
    }

    /**
     * Check if a map contains keys that look like locale identifiers.
     *
     * @param map the map to check
     * @return true if locale-like keys are found
     */
    private boolean containsLocaleKeys(Map<String, Object> map) {
        for (String key : map.keySet()) {
            try {
                DiscordLocale locale = DiscordLocale.from(key.replace("_", "-"));
                // Exclude UNKNOWN locale - it's not a real locale
                if (locale != DiscordLocale.UNKNOWN)
                    return true;
            } catch (Exception ignored) {
                // Not a locale key, continue checking
            }
        }
        return false;
    }
}
