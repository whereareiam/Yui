package me.whereareiam.yui.common.localization.loader;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.configura.Config;
import me.whereareiam.semantica.model.ProviderResult;
import me.whereareiam.semantica.model.translation.TranslationSource;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.translation.base.TranslationProvider;
import me.whereareiam.yui.Constants;
import me.whereareiam.yui.config.ConfigurationTypeResolver;
import me.whereareiam.yui.localization.base.LocalizationLoaderBase;
import me.whereareiam.yui.localization.loader.FileTypeHandlerRegistry;
import me.whereareiam.yui.localization.provider.LocalizationProvider;
import me.whereareiam.yui.type.ConfigurationType;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class YuiTranslationLoader implements TranslationProvider<DiscordLocale> {
    private final FileTypeHandlerRegistry handlerRegistry;
    private final ApplicationContext applicationContext;
    private final ConfigurationTypeResolver configurationTypeResolver;
    private final Path dataPath;

    public YuiTranslationLoader(
            FileTypeHandlerRegistry handlerRegistry,
            ApplicationContext applicationContext,
            ConfigurationTypeResolver configurationTypeResolver,
            @Qualifier("dataPath") Path dataPath
    ) {
        this.handlerRegistry = handlerRegistry;
        this.applicationContext = applicationContext;
        this.configurationTypeResolver = configurationTypeResolver;
        this.dataPath = dataPath;
    }

    @Override
    public ProviderResult<DiscordLocale> provide() {
        Path languagesDir = dataPath.resolve(Constants.Structure.languagesDir);

        boolean empty = !Files.exists(languagesDir) || LocalizationLoaderBase.isEmpty(languagesDir);

        // Generate defaults from LocalizationProvider beans if directory is empty
        if (empty) {
            log.info("[Localization] No translations found, generating defaults");
        }

        // If any provider has applyOnce=false, merge defaults into existing files as well.
        Map<Path, String> prefixOverrides = new HashMap<>();
        generateDefaultsFromProviders(languagesDir, prefixOverrides);

        if (!Files.isDirectory(languagesDir)) {
            log.warn("[Localization] Languages directory not found: {}", languagesDir);
            return new ProviderResult<>(new HashMap<>(), new HashMap<>());
        }

        Map<DiscordLocale, Map<String, TranslationSource>> localized = new HashMap<>();
        Map<String, TemplateEntry> templates = new HashMap<>();

        // Load all localization files using shared utility
        LocalizationLoaderBase.loadFromDirectory(
                languagesDir,
                handlerRegistry,
                // LOCALE processor
                (file, locale, translations) -> {
                    String prefix = resolvePrefix(languagesDir, file, prefixOverrides);
                    // Merge locale translations instead of replacing so multi-locale files (e.g., vocabulary.yml)
                    // are not lost when a per-locale file (e.g., en-US.yml) is loaded afterward.
                    Map<String, TranslationSource> sources = localized.computeIfAbsent(locale, _ -> new HashMap<>());
                    translations.forEach((key, textValue) -> {
                        String fullKey = LocalizationLoaderBase.joinKey(prefix, key);
                        sources.put(fullKey, new TranslationSource(textValue));
                    });
                },
                // MULTI_LOCALE processor
                (file, multiLocaleData) -> {
                    String prefix = resolvePrefix(languagesDir, file, prefixOverrides);
                    log.debug("[Localization] Found {} multi-locale entries in '{}'",
                            multiLocaleData.size(), file.getFileName());

                    multiLocaleData.forEach((key, localeMap) -> {
                        String fullKey = LocalizationLoaderBase.joinKey(prefix, key);
                        localeMap.forEach((locale, textValue) -> {
                            localized.computeIfAbsent(locale, _ -> new HashMap<>())
                                    .put(fullKey, new TranslationSource(textValue));
                        });
                    });
                },
                // TEMPLATE processor
                (file, templateData) -> {
                    String prefix = resolvePrefix(languagesDir, file, prefixOverrides);
                    log.debug("[Localization] Found {} templates in '{}'",
                            templateData.size(), file.getFileName());
                    templateData.forEach((key, textValue) -> {
                        String fullKey = LocalizationLoaderBase.joinKey(prefix, key);
                        templates.put(fullKey, new TemplateEntry(textValue.asString()));
                    });
                }
        );

        int totalKeys = localized.values().stream().mapToInt(Map::size).sum();
        log.info("[Localization] Loaded {} localization keys across {} locales",
                totalKeys, localized.size());
        log.info("[Localization] Loaded {} template entries", templates.size());

        return new ProviderResult<>(localized, templates);
    }

    /**
     * Generate default localization files from discovered LocalizationProvider beans.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void generateDefaultsFromProviders(Path languagesDir, Map<Path, String> prefixOverrides) {
        try {
            Map<String, ?> providerBeans = 
                    applicationContext.getBeansOfType(LocalizationProvider.class);

            if (providerBeans.isEmpty()) {
                log.warn("[Localization] No LocalizationProvider beans found");
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
                        log.debug("[Localization] Skipping existing file: {}", out);
                        continue;
                    }
                    
                    Object model = p.getModelClass().getDeclaredConstructor().newInstance();
                    Object supplied = p.supply(model);

                    if (exists) {
                        Object merged = Config.merge(out, supplied);
                        Config.getDefaultWriter().write(out, merged);
                        log.debug("[Localization] Updated default file: {}", out);
                        continue;
                    }

                    Config.save(out, supplied);
                    log.info("[Localization] Generated default file: {}", out);
                } catch (Exception e) {
                    log.warn("[Localization] Failed to generate defaults from provider", e);
                }
            }
        } catch (Exception e) {
            log.error("[Localization] Failed to generate defaults", e);
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
}
