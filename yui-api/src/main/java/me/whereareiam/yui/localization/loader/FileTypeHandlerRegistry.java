package me.whereareiam.yui.localization.loader;

import me.whereareiam.yui.localization.format.FileFormat;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Registry for file type handlers.
 * Allows registration of custom handlers and lookup by file/format.
 */
public interface FileTypeHandlerRegistry {

    /**
     * Register a file type handler.
     *
     * @param handler the handler to register
     */
    void registerHandler(FileTypeHandler handler);

    /**
     * Find a handler that can handle the given file and format.
     *
     * @param file the file to handle
     * @param format the expected format
     * @return the first matching handler, or empty if none found
     */
    Optional<FileTypeHandler> findHandler(Path file, FileFormat format);

    /**
     * Get all registered handlers.
     *
     * @return immutable list of handlers in registration order
     */
    List<FileTypeHandler> getHandlers();
}
