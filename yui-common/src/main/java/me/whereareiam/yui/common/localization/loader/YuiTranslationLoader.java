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
        generateDefaultsFromProviders(languagesDir);

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
                (_, locale, translations) -> {
                    Map<String, TranslationSource> sources = new HashMap<>();
                    translations.forEach((key, textValue) ->
                            sources.put(key, new TranslationSource(textValue))
                    );
                    localized.put(locale, sources);
                },
                // MULTI_LOCALE processor
                (file, multiLocaleData) -> {
                    log.debug("[Localization] Found {} multi-locale entries in '{}'",
                            multiLocaleData.size(), file.getFileName());

                    multiLocaleData.forEach((key, localeMap) -> {
                        localeMap.forEach((locale, textValue) -> {
                            localized.computeIfAbsent(locale, _ -> new HashMap<>())
                                    .put(key, new TranslationSource(textValue));
                        });
                    });
                },
                // TEMPLATE processor
                (file, templateData) -> {
                    log.debug("[Localization] Found {} templates in '{}'",
                            templateData.size(), file.getFileName());
                    templateData.forEach((key, textValue) -> {
                        templates.put(key, new TemplateEntry(textValue.asString()));
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
    private void generateDefaultsFromProviders(Path languagesDir) {
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
                    String target = p.getTargetFilename();
                    if (target == null) target = p.getDefaultLocale().getLocale();

                    Path out = languagesDir.resolve(target);
                    boolean exists = Files.exists(resolveConfigPath(out, configurationType));

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

    private static Path resolveConfigPath(Path pathWithoutExtension, ConfigurationType configurationType) {
        String raw = pathWithoutExtension.toString();
        String lower = raw.toLowerCase();
        for (ConfigurationType type : ConfigurationType.values())
            if (lower.endsWith(type.getExtension().toLowerCase()))
                return pathWithoutExtension;

        return Path.of(raw + configurationType.getExtension());
    }
}
