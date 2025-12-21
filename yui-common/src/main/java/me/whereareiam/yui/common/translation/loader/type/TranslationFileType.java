package me.whereareiam.yui.common.translation.loader.type;

/**
 * Types of translation files supported by the loader.
 */
public enum TranslationFileType {
    /**
     * Per-locale file: one file represents a single locale with multiple keys.
     * Example: en-US.yml
     * <pre>
     * key1: "value"
     * key2: "value"
     * </pre>
     */
    LOCALE,

    /**
     * Multi-locale file: keys contain maps of locales to translations.
     * Example: vocabulary.yml
     * <pre>
     * key1:
     *   en-US: "value"
     *   de: "Wert"
     * </pre>
     */
    MULTI_LOCALE,

    /**
     * Template file: non-localized text that doesn't vary by locale.
     * Used for configuration values, formats, etc.
     */
    TEMPLATE
}
