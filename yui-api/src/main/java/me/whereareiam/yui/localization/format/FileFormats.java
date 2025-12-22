package me.whereareiam.yui.localization.format;

/**
 * Built-in file format constants.
 */
public final class FileFormats {
    private FileFormats() {}

    /**
     * Single locale file format (e.g., en-US.yml).
     * File name matches a Discord locale identifier.
     */
    public static final FileFormat LOCALE = () -> "LOCALE";

    /**
     * Multi-locale file format (e.g., vocabulary.yml).
     * File contains nested locale maps for each localization key.
     */
    public static final FileFormat MULTI_LOCALE = () -> "MULTI_LOCALE";

    /**
     * Template file format (e.g., templates.yml).
     * File contains template strings with placeholders, no locale association.
     */
    public static final FileFormat TEMPLATE = () -> "TEMPLATE";
}
