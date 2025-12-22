package me.whereareiam.yui.common.localization.format;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.configura.Config;
import me.whereareiam.semantica.model.TextValue;
import me.whereareiam.yui.localization.format.FileFormat;
import me.whereareiam.yui.localization.format.FileFormats;
import me.whereareiam.yui.localization.loader.FileTypeHandler;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler for TEMPLATE format files.
 * Processes template files with placeholders, no locale association.
 */
@Slf4j
public class TemplateFileHandler implements FileTypeHandler {

    @Override
    public boolean canHandle(Path file, FileFormat format) {
        return format.getName().equals(FileFormats.TEMPLATE.getName());
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
            Map<String, TextValue> templates = convertToFlatMap(content);
            
            templateProcessor.process(file, templates);
        } catch (Exception e) {
            log.error("[TemplateFileHandler] Failed to load file: {}", file, e);
        }
    }

    /**
     * Convert nested map to flat TextValue map with dot notation.
     */
    private Map<String, TextValue> convertToFlatMap(Map<String, Object> content) {
        Map<String, TextValue> result = new HashMap<>();
        flattenMap("", content, result);
        return result;
    }

    /**
     * Recursively flatten nested maps with dot notation.
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
