package me.whereareiam.yui.localization.loader;

import me.whereareiam.semantica.model.TextValue;
import me.whereareiam.yui.localization.format.FileFormat;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.nio.file.Path;
import java.util.Map;

/**
 * Handler for loading specific file format types.
 * Implementations inspect files and delegate to appropriate processors.
 */
public interface FileTypeHandler {

    /**
     * Check if this handler can handle the given file and format.
     *
     * @param file the file to check
     * @param format the expected format
     * @return true if this handler supports the file/format combination
     */
    boolean canHandle(Path file, FileFormat format);

    /**
     * Load the file and invoke the appropriate processor callback.
     *
     * @param file the file to load
     * @param localeProcessor callback for LOCALE files
     * @param multiLocaleProcessor callback for MULTI_LOCALE files
     * @param templateProcessor callback for TEMPLATE files
     */
    void load(
            Path file,
            LocaleFileProcessor localeProcessor,
            MultiLocaleFileProcessor multiLocaleProcessor,
            TemplateFileProcessor templateProcessor
    );

    /**
     * Callback for processing LOCALE files.
     */
    @FunctionalInterface
    interface LocaleFileProcessor {
        void process(Path file, DiscordLocale locale, Map<String, TextValue> translations);
    }

    /**
     * Callback for processing MULTI_LOCALE files.
     */
    @FunctionalInterface
    interface MultiLocaleFileProcessor {
        void process(Path file, Map<String, Map<DiscordLocale, TextValue>> multiLocaleData);
    }

    /**
     * Callback for processing TEMPLATE files.
     */
    @FunctionalInterface
    interface TemplateFileProcessor {
        void process(Path file, Map<String, TextValue> templates);
    }
}
