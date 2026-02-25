package me.whereareiam.yui.localization.base;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.configura.Config;
import me.whereareiam.yui.localization.format.FileFormat;
import me.whereareiam.yui.localization.format.FileFormats;
import me.whereareiam.yui.localization.loader.FileTypeHandler;
import me.whereareiam.yui.localization.loader.FileTypeHandlerRegistry;
import me.whereareiam.yui.localization.provider.LocalizationProvider;
import me.whereareiam.yui.type.ConfigurationType;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.stream.Stream;

/**
 * Utility methods for localization loaders.
 */
@Slf4j
public final class LocalizationLoaderBase {
    /**
     * Check if a directory is empty.
     */
    public static boolean isEmpty(Path dir) {
        try (Stream<Path> entries = Files.walk(dir)) {
            return entries.filter(Files::isRegularFile).findAny().isEmpty();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Parse Discord locale from filename (e.g., "en-US.yml" -> DiscordLocale.ENGLISH_US).
     */
    public static DiscordLocale parseLocaleFromFilename(Path file) {
        String filename = file.getFileName().toString();
        String localeName = filename.replaceFirst("\\.[^.]+$", "");

        try {
            DiscordLocale locale = DiscordLocale.from(localeName.replace("_", "-"));
            if (locale == DiscordLocale.UNKNOWN)
                return null;
            return locale;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Merge map {@code src} into {@code target} recursively.
     */
    @SuppressWarnings("unchecked")
    public static void mergeMaps(Map<String, Object> target, Map<String, Object> src) {
        for (Map.Entry<String, Object> e : src.entrySet()) {
            String k = e.getKey();
            Object v = e.getValue();
            if (v instanceof Map && target.get(k) instanceof Map) {
                mergeMaps((Map<String, Object>) target.get(k), (Map<String, Object>) v);
                continue;
            }

            target.put(k, v);
        }
    }

    /**
     * Load all files from a directory using registered handlers.
     */
    public static void loadFromDirectory(
            Path languagesDir,
            FileTypeHandlerRegistry handlerRegistry,
            FileTypeHandler.LocaleFileProcessor localeProcessor,
            FileTypeHandler.MultiLocaleFileProcessor multiLocaleProcessor,
            FileTypeHandler.TemplateFileProcessor templateProcessor
    ) {
        if (!Files.isDirectory(languagesDir))
            return;

        try (Stream<Path> files = Files.walk(languagesDir)) {
            files.filter(Files::isRegularFile)
                    .forEach(file -> {
                        FileFormat format = detectFileFormat(file);
                        Optional<FileTypeHandler> handler = handlerRegistry.findHandler(file, format);

                        if (handler.isPresent()) {
                            handler.get().load(file, localeProcessor, multiLocaleProcessor, templateProcessor);
                        } else {
                            log.warn("[TranslationLoader] No handler found for file: {} with format: {}",
                                    file, format.getName());
                        }
                    });
        } catch (Exception e) {
            log.error("[TranslationLoader] Failed to load from directory: {}", languagesDir, e);
        }
    }

    /**
     * Resolve the target output path (without extension unless already present).
     */
    public static Path resolveTargetPath(
            Path languagesDir,
            LocalizationProvider<?> provider
    ) {
        String targetPath = provider.getTargetPath();
        String targetName = provider.getTargetFilename();
        if (targetName == null || targetName.isBlank())
            targetName = provider.getDefaultLocale().getLocale();
        else if (targetName.contains("/") || targetName.contains("\\")) {
            String normalized = targetName.replace('\\', '/');
            Path namePath = Path.of(normalized).getFileName();
            if (namePath != null) {
                targetName = namePath.toString();
                log.warn("[Localization] getTargetFilename should not include a path. Using filename '{}'.", targetName);
            } else {
                targetName = provider.getDefaultLocale().getLocale();
                log.warn("[Localization] getTargetFilename returned a path without a filename. Using default locale '{}'.", targetName);
            }
        }

        Path out = languagesDir;
        if (targetPath != null && !targetPath.isBlank())
            out = out.resolve(targetPath);
        out = out.resolve(targetName);

        return out;
    }

    /**
     * Resolve config path, adding extension if needed.
     */
    public static Path resolveConfigPath(Path pathWithoutExtension, ConfigurationType configurationType) {
        String raw = pathWithoutExtension.toString();
        String lower = raw.toLowerCase();
        for (ConfigurationType type : ConfigurationType.values())
            if (lower.endsWith(type.getExtension().toLowerCase()))
                return pathWithoutExtension;

        return Path.of(raw + configurationType.getExtension());
    }

    /**
     * Derive key prefix from the parent path relative to the languages directory.
     */
    public static String derivePrefix(Path languagesDir, Path file) {
        if (languagesDir == null || file == null) return "";

        Path normalizedLanguages = languagesDir.toAbsolutePath().normalize();
        Path parent = file.toAbsolutePath().normalize().getParent();
        if (parent == null) return "";

        Path relative;
        try {
            relative = normalizedLanguages.relativize(parent);
        } catch (IllegalArgumentException e) {
            return "";
        }

        if (relative.getNameCount() == 0) return "";

        return StreamSupport.stream(relative.spliterator(), false)
                .map(Path::toString)
                .collect(Collectors.joining("."));
    }

    /**
     * Normalize a prefix by trimming whitespace, converting separators to dots, and
     * removing leading/trailing dots.
     */
    public static String normalizePrefix(String prefix) {
        if (prefix == null)
            return null;

        String trimmed = prefix.trim();
        if (trimmed.isEmpty()) return "";

        trimmed = trimmed.replace('\\', '/');
        while (trimmed.startsWith("/"))
            trimmed = trimmed.substring(1);
        while (trimmed.endsWith("/"))
            trimmed = trimmed.substring(0, trimmed.length() - 1);

        trimmed = trimmed.replace('/', '.');
        while (trimmed.startsWith("."))
            trimmed = trimmed.substring(1);
        while (trimmed.endsWith("."))
            trimmed = trimmed.substring(0, trimmed.length() - 1);

        return trimmed;
    }

    /**
     * Join key parts with dots, ignoring null/blank segments.
     */
    public static String joinKey(String... parts) {
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part == null) continue;

            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;

            int start = 0;
            int end = trimmed.length();
            while (start < end && trimmed.charAt(start) == '.')
                start++;
            while (end > start && trimmed.charAt(end - 1) == '.')
                end--;

            if (end <= start) continue;

            if (!result.isEmpty()) result.append('.');
            result.append(trimmed, start, end);
        }

        return result.toString();
    }

    /**
     * Detect file format based on filename and content.
     */
    public static FileFormat detectFileFormat(Path file) {
        DiscordLocale locale = parseLocaleFromFilename(file);
        if (locale != null) return FileFormats.LOCALE;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> content = Config.load(file, Map.class);
            if (isMultiLocaleStructure(content))
                return FileFormats.MULTI_LOCALE;
        } catch (Exception e) {
            log.debug("[TranslationLoader] Failed to inspect file structure: {}", file, e);
        }

        return FileFormats.TEMPLATE;
    }

    /**
     * Check if content has multi-locale structure.
     */
    @SuppressWarnings("unchecked")
    public static boolean isMultiLocaleStructure(Map<String, Object> content) {
        if (content.isEmpty()) return false;

        int localeMapCount = 0;
        int totalChecked = 0;

        for (Map.Entry<String, Object> entry : content.entrySet()) {
            if (totalChecked >= 5) break;
            totalChecked++;

            if (entry.getValue() instanceof Map) {
                Map<String, Object> valueMap = (Map<String, Object>) entry.getValue();
                if (hasLocaleMapStructure(valueMap))
                    localeMapCount++;
            }
        }

        return localeMapCount > 0;
    }

    @SuppressWarnings("unchecked")
    private static boolean hasLocaleMapStructure(Map<String, Object> map) {
        if (containsLocaleKeys(map)) return true;

        for (Object value : map.values())
            if (value instanceof Map)
                if (containsLocaleKeys((Map<String, Object>) value))
                    return true;

        return false;
    }

    private static boolean containsLocaleKeys(Map<String, Object> map) {
        for (String key : map.keySet()) {
            try {
                DiscordLocale locale = DiscordLocale.from(key.replace("_", "-"));
                if (locale != DiscordLocale.UNKNOWN) return true;
            } catch (Exception ignored) {
            }
        }

        return false;
    }
}
