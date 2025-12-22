package me.whereareiam.yui.localization.format;

/**
 * Represents a localization file format type.
 * Extensible interface allowing custom format implementations.
 */
public interface FileFormat {
    /**
     * @return unique name of this format
     */
    String getName();
}
