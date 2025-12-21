package me.whereareiam.yui.common.translation.loader;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.configura.Config;
import me.whereareiam.semantica.model.TextValue;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Abstract base class for loading translations from flat format files.
 * Provides common functionality for:
 * - Loading and parsing config files (respects Configura's format configuration)
 * - Converting flat format (nested maps) to TextValues with dot notation
 * - Iterating through translation directories
 * - Parsing locale names from filenames
 * <p>
 * Only files matching the configured format (YAML/JSON) are loaded.
 */
@Slf4j
public abstract class AbstractTranslationLoader {
    /**
     * Load all translation files from a directory.
     * Iterates through all files matching the configured format and delegates processing to subclasses.
     * Files that map to valid locales are treated as localized translations.
     * Files that cannot be mapped to locales are treated as templates.
     *
     * @param languagesDir directory containing translation files
     * @param localeProcessor callback to process locale files
     * @param templateProcessor callback to process template files
     */
    protected void loadFromDirectory(
            Path languagesDir, 
            TranslationFileProcessor localeProcessor,
            TemplateFileProcessor templateProcessor
    ) {
        if (!Files.isDirectory(languagesDir)) {
            log.debug("[TranslationLoader] Directory not found: {}", languagesDir);
            return;
        }

        // Get the file extension from Configura's format configuration
        String extension = getConfiguredExtension();

        try (Stream<Path> files = Files.list(languagesDir)) {
            files.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(extension))
                    .forEach(file -> {
                        DiscordLocale locale = parseLocaleFromFilename(file);
                        Map<String, TextValue> translations = loadAndConvertFile(file);
                        
                        if (locale != null) {
                            // Valid locale file - process as localized translations
                            localeProcessor.process(file, locale, translations);
                        } else {
                            // Cannot map to locale - treat as templates
                            log.debug("[TranslationLoader] File '{}' not mappable to locale, treating as templates", file.getFileName());
                            templateProcessor.process(file, translations);
                        }
                    });
        } catch (Exception e) {
            log.error("[TranslationLoader] Failed to load from directory: {}", languagesDir, e);
        }
    }
    
    /**
     * Get the file extension based on Configura's configured format.
     *
     * @return file extension (e.g., ".yml", ".json")
     */
    private String getConfiguredExtension() {
        return switch (Config.getDefaultReader().getFormat()) {
            case YAML -> ".yml";
            case JSON -> ".json";
        };
    }
    
    /**
     * Load a file and convert it to a map of key -> TextValue.
     * Supports nested maps and flattens them with dot notation.
     *
     * @param file the file to load
     * @return map of translation keys to TextValues
     */
    protected Map<String, TextValue> loadAndConvertFile(Path file) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> flatMap = Config.load(file, Map.class);
            return convertFlatFormat(flatMap);
        } catch (Exception e) {
            log.error("[TranslationLoader] Failed to load file: {}", file, e);
            return new HashMap<>();
        }
    }
    
    /**
     * Convert flat format map to TextValue translations.
     * Supports nested maps and flattens them with dot notation.
     *
     * @param flatMap the flat format map
     * @return map of translation keys to TextValues
     */
    private Map<String, TextValue> convertFlatFormat(Map<String, Object> flatMap) {
        Map<String, TextValue> result = new HashMap<>();
        flattenMap("", flatMap, result);
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
                // Recursively flatten nested maps
                flattenMap(fullKey, (Map<String, Object>) value, result);
            } else {
                // Convert value to TextValue
                result.put(fullKey, TextValue.from(value));
            }
        });
    }
    
    /**
     * Parse Discord locale from filename (e.g., "en-US.yml" -> DiscordLocale.ENGLISH_US).
     *
     * @param file the file whose name to parse
     * @return parsed DiscordLocale, or null if invalid
     */
    protected DiscordLocale parseLocaleFromFilename(Path file) {
        String filename = file.getFileName().toString();
        String localeName = filename.replaceFirst("\\.[^.]+$", "");
        
        try {
            return DiscordLocale.from(localeName.replace("_", "-"));
        } catch (Exception e) {
            log.warn("[TranslationLoader] Invalid locale in filename: {}", filename);
            return null;
        }
    }
    
    /**
     * Check if a directory is empty.
     *
     * @param dir directory to check
     * @return true if empty or doesn't exist
     */
    protected boolean isEmpty(Path dir) {
        try (Stream<Path> entries = Files.list(dir)) {
            return entries.findAny().isEmpty();
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * Functional interface for processing translation files.
     */
    protected interface TranslationFileProcessor {
        void process(Path file, DiscordLocale locale, Map<String, TextValue> translations);
    }
    
    /**
     * Functional interface for processing template files.
     */
    protected interface TemplateFileProcessor {
        void process(Path file, Map<String, TextValue> templates);
    }
}
