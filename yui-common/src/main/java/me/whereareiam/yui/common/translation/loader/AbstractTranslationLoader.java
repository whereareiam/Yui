package me.whereareiam.yui.common.translation.loader;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.configura.Config;
import me.whereareiam.semantica.model.TextValue;
import me.whereareiam.yui.common.translation.loader.type.*;
import me.whereareiam.yui.common.translation.loader.type.LocaleFileType;
import me.whereareiam.yui.common.translation.loader.type.TemplateFileType;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Abstract base class for loading translations from flat format files.
 * Provides common functionality for:
 * - Iterating through translation directories
 * - Detecting translation file types (LOCALE, MULTI_LOCALE, TEMPLATE)
 * - Delegating to specific file type handlers
 * <p>
 * Only files matching the configured format (YAML/JSON) are loaded.
 */
@Slf4j
public abstract class AbstractTranslationLoader {
    private final LocaleFileType localeHandler = new LocaleFileType();
    private final MultiLocaleFileType multiLocaleHandler = new MultiLocaleFileType();
    private final TemplateFileType templateHandler = new TemplateFileType();

    /**
     * Load all translation files from a directory.
     * Iterates through all files matching the configured format and delegates processing to subclasses.
     * Automatically detects file type based on filename and content structure.
     *
     * @param languagesDir directory containing translation files
     * @param localeProcessor callback to process LOCALE files
     * @param multiLocaleProcessor callback to process MULTI_LOCALE files
     * @param templateProcessor callback to process TEMPLATE files
     */
    protected void loadFromDirectory(
            Path languagesDir, 
            LocaleFileProcessor localeProcessor,
            MultiLocaleFileProcessor multiLocaleProcessor,
            TemplateFileProcessor templateProcessor
    ) {
        if (!Files.isDirectory(languagesDir)) {
            log.debug("[TranslationLoader] Directory not found: {}", languagesDir);
            return;
        }

        String extension = getConfiguredExtension();

        try (Stream<Path> files = Files.list(languagesDir)) {
            files.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(extension))
                    .forEach(file -> {
                        TranslationFileType fileType = detectFileType(file);
                        
                        switch (fileType) {
                            case LOCALE -> {
                                DiscordLocale locale = parseLocaleFromFilename(file);
                                Map<String, TextValue> translations = localeHandler.load(file);
                                localeProcessor.process(file, locale, translations);
                            }
                            case MULTI_LOCALE -> {
                                Map<String, Map<DiscordLocale, TextValue>> multiLocaleData = multiLocaleHandler.load(file);
                                multiLocaleProcessor.process(file, multiLocaleData);
                            }
                            case TEMPLATE -> {
                                Map<String, TextValue> templates = templateHandler.load(file);
                                templateProcessor.process(file, templates);
                            }
                        }
                    });
        } catch (Exception e) {
            log.error("[TranslationLoader] Failed to load from directory: {}", languagesDir, e);
        }
    }
    
    /**
     * Detect the type of translation file based on filename and content.
     *
     * @param file the file to detect
     * @return detected file type
     */
    private TranslationFileType detectFileType(Path file) {
        // First check if filename matches a Discord locale
        DiscordLocale locale = parseLocaleFromFilename(file);
        if (locale != null) {
            return TranslationFileType.LOCALE;
        }
        
        // Load file and inspect structure
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> content = Config.load(file, Map.class);
            
            if (isMultiLocaleStructure(content)) {
                return TranslationFileType.MULTI_LOCALE;
            }
        } catch (Exception e) {
            log.warn("[TranslationLoader] Failed to inspect file structure: {}", file, e);
        }
        
        // Default to template
        return TranslationFileType.TEMPLATE;
    }
    
    /**
     * Check if the loaded content has a multi-locale structure.
     * Multi-locale structure: top-level values are maps containing locale keys.
     *
     * @param content loaded file content
     * @return true if multi-locale structure detected
     */
    @SuppressWarnings("unchecked")
    private boolean isMultiLocaleStructure(Map<String, Object> content) {
        if (content.isEmpty()) {
            return false;
        }
        
        // Check if at least one top-level entry has locale map structure
        // We need to check nested maps too for structures like "category.utility"
        int localeMapCount = 0;
        int totalChecked = 0;
        
        for (Map.Entry<String, Object> entry : content.entrySet()) {
            if (totalChecked >= 5) break; // Check up to 5 entries
            totalChecked++;
            
            if (entry.getValue() instanceof Map) {
                Map<String, Object> valueMap = (Map<String, Object>) entry.getValue();
                
                // Check if this map or any nested map contains locale keys
                if (hasLocaleMapStructure(valueMap)) {
                    localeMapCount++;
                }
            }
        }
        
        log.debug("[TranslationLoader] Structure check: {}/{} entries have locale maps", 
                localeMapCount, totalChecked);
        
        // If at least one entry has locale maps, consider it MULTI_LOCALE
        return localeMapCount > 0;
    }
    
    /**
     * Check if a map has locale map structure (directly or in nested values).
     */
    @SuppressWarnings("unchecked")
    private boolean hasLocaleMapStructure(Map<String, Object> map) {
        // First check if this map directly contains locale keys
        if (containsLocaleKeys(map)) {
            return true;
        }
        
        // Check nested maps (for structures like category.utility)
        for (Object value : map.values()) {
            if (value instanceof Map) {
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                if (containsLocaleKeys(nestedMap)) {
                    return true;
                }
            }
        }
        
        return false;
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
                DiscordLocale.from(key.replace("_", "-"));
                return true;
            } catch (Exception ignored) {
                // Not a locale key, continue checking
            }
        }
        return false;
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
     * Parse Discord locale from filename (e.g., "en-US.yml" -> DiscordLocale.ENGLISH_US).
     *
     * @param file the file whose name to parse
     * @return parsed DiscordLocale, or null if invalid
     */
    protected DiscordLocale parseLocaleFromFilename(Path file) {
        String filename = file.getFileName().toString();
        String localeName = filename.replaceFirst("\\.[^.]+$", "");
        
        try {
            DiscordLocale locale = DiscordLocale.from(localeName.replace("_", "-"));
            
            // Exclude UNKNOWN locale - it's not a real locale
            if (locale == DiscordLocale.UNKNOWN)
                return null;
            
            return locale;
        } catch (Exception e) {
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
     * Functional interface for processing LOCALE files.
     */
    protected interface LocaleFileProcessor {
        void process(Path file, DiscordLocale locale, Map<String, TextValue> translations);
    }
    
    /**
     * Functional interface for processing MULTI_LOCALE files.
     */
    protected interface MultiLocaleFileProcessor {
        void process(Path file, Map<String, Map<DiscordLocale, TextValue>> multiLocaleData);
    }
    
    /**
     * Functional interface for processing TEMPLATE files.
     */
    protected interface TemplateFileProcessor {
        void process(Path file, Map<String, TextValue> templates);
    }
}
