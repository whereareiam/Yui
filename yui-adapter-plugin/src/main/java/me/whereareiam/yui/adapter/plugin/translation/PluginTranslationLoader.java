package me.whereareiam.yui.adapter.plugin.translation;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.configura.Config;
import me.whereareiam.semantica.locale.LocaleParser;
import me.whereareiam.semantica.model.translation.entry.LocalizedEntry;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.translation.TranslationService;
import me.whereareiam.semantica.translation.base.TranslationLocale;
import me.whereareiam.yui.Constants;
import me.whereareiam.yui.config.ConfigurationTypeResolver;
import me.whereareiam.yui.localization.base.LocalizationLoaderBase;
import me.whereareiam.yui.localization.loader.FileTypeHandlerRegistry;
import me.whereareiam.yui.localization.provider.LocalizationProvider;
import me.whereareiam.yui.model.plugin.InternalPlugin;
import me.whereareiam.yui.type.ConfigurationType;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Loads translations for plugins using registered file type handlers.
 * Discovers LocalizationProvider beans from plugin contexts and generates defaults on first load.
 */
@Slf4j
@Component
public class PluginTranslationLoader {
    private final TranslationService<DiscordLocale> translationService;
    private final Path pluginsPath;
    private final LocaleParser<DiscordLocale> localeParser;
    private final FileTypeHandlerRegistry handlerRegistry;
    private final ConfigurationTypeResolver configurationTypeResolver;

    public PluginTranslationLoader(
            TranslationService<DiscordLocale> translationService,
            @Qualifier("pluginsPath") Path pluginsPath,
            LocaleParser<DiscordLocale> localeParser,
            FileTypeHandlerRegistry handlerRegistry,
            ConfigurationTypeResolver configurationTypeResolver
    ) {
        this.translationService = translationService;
        this.pluginsPath = pluginsPath;
        this.localeParser = localeParser;
        this.handlerRegistry = handlerRegistry;
        this.configurationTypeResolver = configurationTypeResolver;
    }

    public void loadPlugin(InternalPlugin plugin) {
        if (plugin == null) return;
        String pluginId = plugin.getPlugin().getId();
        if (pluginId == null || pluginId.isBlank()) return;

        String pluginName = plugin.getPlugin().getName();
        // Use plugin name for directory, sanitized for filesystem
        String pluginDirName = plugin.getPlugin().getName().replaceAll("[^a-zA-Z0-9.-]", "_");

        Path languagesDir = pluginsPath
                .resolve(pluginDirName)
                .resolve(Constants.Structure.languagesDir);

        boolean empty = !Files.exists(languagesDir) || LocalizationLoaderBase.isEmpty(languagesDir);
        if (empty) log.info("[Localization] No translations found for plugin '{}', generating defaults", pluginName);

        // Always apply provider defaults so applyOnce=false providers can merge updates into existing files.
        Map<Path, String> prefixOverrides = new HashMap<>();
        generateDefaultsFromProviders(plugin, languagesDir, prefixOverrides);

        if (!Files.isDirectory(languagesDir)) {
            log.debug("[Localization] No translations for plugin: {}", pluginName);
            return;
        }

        String pluginPrefix = "plugin." + pluginId.toLowerCase();
        Map<String, Map<TranslationLocale, String>> accumulator = new HashMap<>();
        AtomicInteger templateCount = new AtomicInteger(0);

        // Load all localization files using shared utility
        LocalizationLoaderBase.loadFromDirectory(
                languagesDir,
                handlerRegistry,
                // LOCALE processor
                (file, locale, translations) -> {
                    String prefix = resolvePrefix(languagesDir, file, prefixOverrides);
                    TranslationLocale translationLocale = localeParser.parse(locale);
                    translations.forEach((key, textValue) -> {
                        String fullKey = LocalizationLoaderBase.joinKey(pluginPrefix, prefix, key);
                        accumulator.computeIfAbsent(fullKey, _ -> new HashMap<>())
                                .put(translationLocale, textValue.asString());
                    });
                },
                // MULTI_LOCALE processor
                (file, multiLocaleData) -> {
                    String prefix = resolvePrefix(languagesDir, file, prefixOverrides);
                    multiLocaleData.forEach((key, localeMap) -> {
                        String fullKey = LocalizationLoaderBase.joinKey(pluginPrefix, prefix, key);
                        Map<TranslationLocale, String> translations = new HashMap<>();
                        localeMap.forEach((locale, textValue) -> {
                            translations.put(localeParser.parse(locale), textValue.asString());
                        });
                        accumulator.computeIfAbsent(fullKey, _ -> new HashMap<>()).putAll(translations);
                    });
                    log.info("[Localization] Loaded {} multi-locale entries from '{}' for plugin: {}",
                            multiLocaleData.size(), file.getFileName(), pluginName);
                },
                // TEMPLATE processor
                (file, templates) -> {
                    String prefix = resolvePrefix(languagesDir, file, prefixOverrides);
                    templates.forEach((key, textValue) -> {
                        String fullKey = LocalizationLoaderBase.joinKey(pluginPrefix, prefix, key);
                        translationService.register(fullKey, new TemplateEntry(textValue.asString()));
                        templateCount.incrementAndGet();
                    });
                    log.info("[Localization] Loaded {} templates from '{}' for plugin: {}",
                            templates.size(), file.getFileName(), pluginName);
                }
        );

        // Register all accumulated localized entries
        accumulator.forEach((key, translations) -> {
            translationService.register(key, new LocalizedEntry(translations));
        });

        log.info("[Localization] Loaded {} localized keys and {} template entries for plugin: {}",
                accumulator.size(), templateCount.get(), pluginName);
    }

    /**
     * Generate default localization files from discovered LocalizationProvider beans.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void generateDefaultsFromProviders(
            InternalPlugin plugin,
            Path languagesDir,
            Map<Path, String> prefixOverrides
    ) {
        String pluginName = plugin.getPlugin().getName();

        try {
            Map<String, ?> providerBeans =
                    plugin.getContext().getBeansOfType(LocalizationProvider.class);

            if (providerBeans.isEmpty()) {
                log.debug("[Localization] No LocalizationProvider beans found for plugin: {}", pluginName);
                return;
            }

            Files.createDirectories(languagesDir);
            ConfigurationType configurationType = configurationTypeResolver.getConfigurationType();

            // Generate file per provider
            for (Object bean : providerBeans.values()) {
                LocalizationProvider p = (LocalizationProvider) bean;
                
                try {
                    Path out = LocalizationLoaderBase.resolveTargetPath(languagesDir, p);
                    Path configPath = LocalizationLoaderBase.resolveConfigPath(out, configurationType);
                    boolean exists = Files.exists(configPath);

                    registerPrefixOverride(prefixOverrides, configPath, p.getKeyPrefix());

                    if (configPath.getParent() != null)
                        Files.createDirectories(configPath.getParent());

                    // Skip if file exists and provider should only apply once
                    if (p.applyOnce() && exists) {
                        log.debug("[Localization] Skipping existing file: {} for plugin: {}", out.getFileName(), pluginName);
                        continue;
                    }
                    
                    Object model = p.getModelClass().getDeclaredConstructor().newInstance();
                    Object supplied = p.supply(model);

                    if (exists) {
                        Object merged = Config.merge(out, supplied);
                        Config.getDefaultWriter().write(out, merged);
                        log.debug("[Localization] Updated default file '{}' for plugin: {}", out.getFileName(), pluginName);
                        continue;
                    }

                    Config.save(out, supplied);
                    log.info("[Localization] Generated default file '{}' for plugin: {}", out.getFileName(), pluginName);
                } catch (Exception e) {
                    log.warn("[Localization] Failed to generate defaults from provider for plugin: {}", pluginName, e);
                }
            }
        } catch (Exception e) {
            log.warn("[Localization] Failed to generate plugin defaults for plugin: {}", pluginName, e);
        }
    }

    private static String resolvePrefix(Path languagesDir, Path file, Map<Path, String> prefixOverrides) {
        Path normalized = file.toAbsolutePath().normalize();
        if (prefixOverrides.containsKey(normalized)) {
            return LocalizationLoaderBase.normalizePrefix(prefixOverrides.get(normalized));
        }

        return LocalizationLoaderBase.derivePrefix(languagesDir, file);
    }

    private static void registerPrefixOverride(
            Map<Path, String> prefixOverrides,
            Path configPath,
            String prefix
    ) {
        if (prefix == null) return;

        String normalized = LocalizationLoaderBase.normalizePrefix(prefix);
        Path normalizedPath = configPath.toAbsolutePath().normalize();
        prefixOverrides.put(normalizedPath, normalized);
    }

    public void unloadPlugin(InternalPlugin plugin) {
        if (plugin == null) return;
        String pluginId = plugin.getPlugin().getId();
        if (pluginId == null || pluginId.isBlank()) return;

        String prefix = "plugin." + pluginId.toLowerCase() + ".";
        int removed = 0;

        for (String key : translationService.getKeys(prefix)) {
            translationService.unregister(key);
            removed++;
        }

        if (removed > 0) {
            log.info("[Localization] Unloaded {} keys for plugin: {}", removed, plugin.getPlugin().getName());
        }
    }
}
