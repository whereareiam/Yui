package me.whereareiam.yui.adapter.plugin.translation;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.configura.Config;
import me.whereareiam.semantica.locale.LocaleParser;
import me.whereareiam.semantica.model.translation.entry.LocalizedEntry;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.translation.TranslationService;
import me.whereareiam.semantica.translation.base.TranslationLocale;
import me.whereareiam.yui.Constants;
import me.whereareiam.yui.model.plugin.InternalPlugin;
import me.whereareiam.yui.localization.base.LocalizationLoaderBase;
import me.whereareiam.yui.localization.loader.FileTypeHandlerRegistry;
import me.whereareiam.yui.localization.provider.LocalizationProvider;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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

    public PluginTranslationLoader(
            TranslationService<DiscordLocale> translationService,
            @Qualifier("pluginsPath") Path pluginsPath,
            LocaleParser<DiscordLocale> localeParser,
            FileTypeHandlerRegistry handlerRegistry
    ) {
        this.translationService = translationService;
        this.pluginsPath = pluginsPath;
        this.localeParser = localeParser;
        this.handlerRegistry = handlerRegistry;
    }

    public void loadPlugin(InternalPlugin plugin) {
        if (plugin == null) return;
        String pluginId = plugin.getPlugin().getId();
        if (pluginId == null || pluginId.isBlank()) return;

        // Use plugin name for directory, sanitized for filesystem
        String pluginDirName = plugin.getPlugin().getName().replaceAll("[^a-zA-Z0-9.-]", "_");

        Path languagesDir = pluginsPath
                .resolve(pluginDirName)
                .resolve(Constants.Structure.languagesDir);

        // Generate defaults from LocalizationProvider beans if directory is empty
        if (!Files.exists(languagesDir) || LocalizationLoaderBase.isEmpty(languagesDir))
            generateDefaultsFromProviders(plugin, languagesDir, pluginId);

        if (!Files.isDirectory(languagesDir)) {
            log.debug("[Localization] No translations for plugin: {}", pluginId);
            return;
        }

        String prefix = "plugin." + pluginId.toLowerCase() + ".";
        Map<String, Map<TranslationLocale, String>> accumulator = new HashMap<>();
        AtomicInteger templateCount = new AtomicInteger(0);

        // Load all localization files using shared utility
        LocalizationLoaderBase.loadFromDirectory(
                languagesDir,
                handlerRegistry,
                // LOCALE processor
                (_, locale, translations) -> {
                    TranslationLocale translationLocale = localeParser.parse(locale);
                    translations.forEach((key, textValue) -> {
                        String fullKey = prefix + key;
                        accumulator.computeIfAbsent(fullKey, _ -> new HashMap<>())
                                .put(translationLocale, textValue.asString());
                    });
                },
                // MULTI_LOCALE processor
                (file, multiLocaleData) -> {
                    multiLocaleData.forEach((key, localeMap) -> {
                        String fullKey = prefix + key;
                        Map<TranslationLocale, String> translations = new HashMap<>();
                        localeMap.forEach((locale, textValue) -> {
                            translations.put(localeParser.parse(locale), textValue.asString());
                        });
                        accumulator.computeIfAbsent(fullKey, _ -> new HashMap<>()).putAll(translations);
                    });
                    log.info("[Localization] Loaded {} multi-locale entries from '{}' for plugin: {}",
                            multiLocaleData.size(), file.getFileName(), pluginId);
                },
                // TEMPLATE processor
                (file, templates) -> {
                    templates.forEach((key, textValue) -> {
                        String fullKey = prefix + key;
                        translationService.register(fullKey, new TemplateEntry(textValue.asString()));
                        templateCount.incrementAndGet();
                    });
                    log.info("[Localization] Loaded {} templates from '{}' for plugin: {}",
                            templates.size(), file.getFileName(), pluginId);
                }
        );

        // Register all accumulated localized entries
        accumulator.forEach((key, translations) -> {
            translationService.register(key, new LocalizedEntry(translations));
        });

        log.info("[Localization] Loaded {} localized keys and {} template entries for plugin: {}",
                accumulator.size(), templateCount.get(), pluginId);
    }

    /**
     * Generate default localization files from discovered LocalizationProvider beans.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void generateDefaultsFromProviders(InternalPlugin plugin, Path languagesDir, String pluginId) {
        try {
            Map<String, ?> providerBeans =
                    plugin.getContext().getBeansOfType(LocalizationProvider.class);

            if (providerBeans.isEmpty()) {
                log.debug("[Localization] No LocalizationProvider beans found for plugin: {}", pluginId);
                return;
            }

            Files.createDirectories(languagesDir);

            // Generate file per provider
            for (Object bean : providerBeans.values()) {
                LocalizationProvider p = (LocalizationProvider) bean;
                
                try {
                    String target = p.getTargetFilename();
                    if (target == null) target = p.getDefaultLocale().getLocale();
                    
                    Path out = languagesDir.resolve(target);
                    
                    // Skip if file exists and provider should only apply once
                    if (p.applyOnce() && Files.exists(out)) {
                        log.debug("[Localization] Skipping existing file: {} for plugin: {}", out.getFileName(), pluginId);
                        continue;
                    }
                    
                    Object model = p.getModelClass().getDeclaredConstructor().newInstance();
                    Object supplied = p.supply(model);
                    
                    Config.save(out, supplied);
                    log.info("[Localization] Generated default file '{}' for plugin: {}", out.getFileName(), pluginId);
                } catch (Exception e) {
                    log.warn("[Localization] Failed to generate defaults from provider for plugin: {}", pluginId, e);
                }
            }
        } catch (Exception e) {
            log.warn("[Localization] Failed to generate plugin defaults for plugin: {}", pluginId, e);
        }
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
            log.info("[Localization] Unloaded {} keys for plugin: {}", removed, pluginId);
        }
    }
}
