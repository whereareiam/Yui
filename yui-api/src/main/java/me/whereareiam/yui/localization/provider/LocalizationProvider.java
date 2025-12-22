package me.whereareiam.yui.localization.provider;

import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.yui.localization.format.FileFormat;
import net.dv8tion.jda.api.interactions.DiscordLocale;

/**
 * Provider for model-backed localization files.
 * Implementations supply a model instance that will be serialized to a localization file.
 * Providers are discovered automatically from Spring ApplicationContexts and applied
 * once when localization files are missing.
 *
 * @param <T> the model type
 */
public interface LocalizationProvider<T> extends TemplateProvider<T> {
    /**
     * @return the model class that will be instantiated and passed to {@link #supply(Object)}
     */
    Class<T> getModelClass();

    /**
     * @return the file format for this provider (LOCALE, MULTI_LOCALE, or TEMPLATE)
     */
    FileFormat getFormat();

    /**
     * Optional target filename (without extension). If present, the loader will create
     * a file named {@code languages/<targetFilename>} (e.g., "vocabulary").
     * If null, {@link #getDefaultLocale()} is used to determine the filename.
     *
     * @return custom filename or null to use locale-based naming
     */
    default String getTargetFilename() {
        return null;
    }

    /**
     * Default locale to use when {@link #getTargetFilename()} returns null.
     * The file will be named {@code languages/<locale>} (e.g., "en-US").
     *
     * @return the default locale (default: en-US)
     */
    default DiscordLocale getDefaultLocale() {
        return DiscordLocale.ENGLISH_US;
    }

    /**
     * If true, the provider will be applied only once when creating missing files.
     * Subsequent loads will not re-apply defaults if files already exist.
     *
     * @return true to apply once (default: true)
     */
    default boolean applyOnce() {
        return true;
    }
}
