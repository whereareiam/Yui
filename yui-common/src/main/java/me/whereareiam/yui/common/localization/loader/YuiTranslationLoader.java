package me.whereareiam.yui.common.localization.loader;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.configura.Config;
import me.whereareiam.semantica.model.ProviderResult;
import me.whereareiam.semantica.model.translation.TranslationSource;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.translation.base.TranslationProvider;
import me.whereareiam.yui.Constants;
import me.whereareiam.yui.localization.base.LocalizationLoaderBase;
import me.whereareiam.yui.localization.loader.FileTypeHandlerRegistry;
import me.whereareiam.yui.localization.provider.LocalizationProvider;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@Component
public class YuiTranslationLoader implements TranslationProvider<DiscordLocale> {
    private final FileTypeHandlerRegistry handlerRegistry;
    private final ApplicationContext applicationContext;
    private final Path dataPath;

    public YuiTranslationLoader(
            FileTypeHandlerRegistry handlerRegistry,
            ApplicationContext applicationContext,
            @Qualifier("dataPath") Path dataPath
    ) {
        this.handlerRegistry = handlerRegistry;
        this.applicationContext = applicationContext;
        this.dataPath = dataPath;
    }

    @Override
    public ProviderResult<DiscordLocale> provide() {
        Path languagesDir = dataPath.resolve(Constants.Structure.languagesDir);

        // Generate defaults from LocalizationProvider beans if directory is empty
        if (!Files.exists(languagesDir) || LocalizationLoaderBase.isEmpty(languagesDir)) {
            log.info("[localization] No translations found, generating defaults");
            generateDefaultsFromProviders(languagesDir);
        }

        if (!Files.isDirectory(languagesDir)) {
            log.warn("[localization] Languages directory not found: {}", languagesDir);
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
                    log.debug("[localization] Found {} multi-locale entries in '{}'",
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
                    log.debug("[localization] Found {} templates in '{}'",
                            templateData.size(), file.getFileName());
                    templateData.forEach((key, textValue) -> {
                        templates.put(key, new TemplateEntry(textValue.asString()));
                    });
                }
        );

        int totalKeys = localized.values().stream().mapToInt(Map::size).sum();
        log.info("[localization] Loaded {} localization keys across {} locales",
                totalKeys, localized.size());
        log.info("[localization] Loaded {} template entries", templates.size());

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
                log.warn("[localization] No LocalizationProvider beans found");
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
                        log.debug("[localization] Skipping existing file: {}", out);
                        continue;
                    }
                    
                    Object model = p.getModelClass().getDeclaredConstructor().newInstance();
                    Object supplied = p.supply(model);
                    
                    Config.save(out, supplied);
                    log.info("[localization] Generated default file: {}", out);
                } catch (Exception e) {
                    log.warn("[localization] Failed to generate defaults from provider", e);
                }
            }
        } catch (Exception e) {
            log.error("[localization] Failed to generate defaults", e);
        }
    }
}
