package me.whereareiam.yui.common.localization.format;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.configura.Config;
import me.whereareiam.semantica.model.TextValue;
import me.whereareiam.yui.localization.base.LocalizationLoaderBase;
import me.whereareiam.yui.localization.format.FileFormat;
import me.whereareiam.yui.localization.format.FileFormats;
import me.whereareiam.yui.localization.loader.FileTypeHandler;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler for LOCALE format files.
 * Processes per-locale files where filename matches a Discord locale (e.g., en-US.yml).
 */
@Slf4j
public class LocaleFileHandler implements FileTypeHandler {
    @Override
    public boolean canHandle(Path file, FileFormat format) {
        if (!format.getName().equals(FileFormats.LOCALE.getName()))
            return false;

        // Check if filename matches a valid Discord locale
        DiscordLocale locale = LocalizationLoaderBase.parseLocaleFromFilename(file);
        return locale != null;
    }

    @Override
    public void load(
            Path file,
            LocaleFileProcessor localeProcessor,
            MultiLocaleFileProcessor multiLocaleProcessor,
            TemplateFileProcessor templateProcessor
    ) {
        try {
            DiscordLocale locale = LocalizationLoaderBase.parseLocaleFromFilename(file);
            if (locale == null) {
                log.warn("[LocaleFileHandler] Cannot parse locale from filename: {}", file);
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> content = Config.load(file, Map.class);
            Map<String, TextValue> translations = convertToFlatMap(content);
            
            localeProcessor.process(file, locale, translations);
        } catch (Exception e) {
            log.error("[LocaleFileHandler] Failed to load file: {}", file, e);
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
