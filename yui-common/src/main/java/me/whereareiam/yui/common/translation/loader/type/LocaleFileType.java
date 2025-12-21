package me.whereareiam.yui.common.translation.loader.type;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.configura.Config;
import me.whereareiam.semantica.model.TextValue;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler for LOCALE type files.
 * Processes per-locale files where filename matches a Discord locale (e.g., en-US.yml).
 */
@Slf4j
public class LocaleFileType {
    /**
     * Load a locale file and convert to flat TextValue map.
     *
     * @param file the file to load
     * @return map of translation keys to TextValues
     */
    public Map<String, TextValue> load(Path file) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> content = Config.load(file, Map.class);
            return convertToFlatMap(content);
        } catch (Exception e) {
            log.error("[LocaleFileType] Failed to load file: {}", file, e);
            return new HashMap<>();
        }
    }

    /**
     * Convert nested map to flat TextValue map with dot notation.
     *
     * @param content the loaded content
     * @return flattened map
     */
    private Map<String, TextValue> convertToFlatMap(Map<String, Object> content) {
        Map<String, TextValue> result = new HashMap<>();
        flattenMap("", content, result);
        return result;
    }

    /**
     * Recursively flatten nested maps with dot notation.
     *
     * @param prefix current key prefix
     * @param map map to flatten
     * @param result accumulator for flattened results
     */
    @SuppressWarnings("unchecked")
    private void flattenMap(String prefix, Map<String, Object> map, Map<String, TextValue> result) {
        map.forEach((key, value) -> {
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

            if (value instanceof Map) {
                flattenMap(fullKey, (Map<String, Object>) value, result);
                return;
            }

            result.put(fullKey, TextValue.from(value));
        });
    }
}
