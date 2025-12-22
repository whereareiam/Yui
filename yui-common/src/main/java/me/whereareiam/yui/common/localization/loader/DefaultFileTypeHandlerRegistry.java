package me.whereareiam.yui.common.localization.loader;

import me.whereareiam.yui.localization.format.FileFormat;
import me.whereareiam.yui.localization.loader.FileTypeHandler;
import me.whereareiam.yui.localization.loader.FileTypeHandlerRegistry;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default thread-safe implementation of FileTypeHandlerRegistry.
 */
public class DefaultFileTypeHandlerRegistry implements FileTypeHandlerRegistry {
    private final List<FileTypeHandler> handlers = new CopyOnWriteArrayList<>();

    @Override
    public void registerHandler(FileTypeHandler handler) {
        if (handler != null && !handlers.contains(handler))
            handlers.add(handler);
    }

    @Override
    public Optional<FileTypeHandler> findHandler(Path file, FileFormat format) {
        return handlers.stream()
                .filter(h -> h.canHandle(file, format))
                .findFirst();
    }

    @Override
    public List<FileTypeHandler> getHandlers() {
        return new ArrayList<>(handlers);
    }
}
