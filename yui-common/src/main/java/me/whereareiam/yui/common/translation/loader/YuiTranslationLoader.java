package me.whereareiam.yui.common.translation.loader;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.configura.Config;
import me.whereareiam.semantica.model.translation.TranslationSource;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.translation.TranslationService;
import me.whereareiam.semantica.translation.base.TranslationProvider;
import me.whereareiam.yui.Constants;
import me.whereareiam.yui.common.config.template.MessagesTemplate;
import me.whereareiam.yui.model.config.messages.Messages;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class YuiTranslationLoader extends AbstractTranslationLoader implements TranslationProvider<DiscordLocale> {
    private final Path dataPath;
    private final MessagesTemplate messagesTemplate;

    public YuiTranslationLoader(
            @Qualifier("dataPath") Path dataPath,
            MessagesTemplate messagesTemplate
    ) {
        this.dataPath = dataPath;
        this.messagesTemplate = messagesTemplate;
    }

    @Override
    public Map<DiscordLocale, Map<String, TranslationSource>> provideAll() {
        Path languagesDir = dataPath.resolve(Constants.Structure.languagesDir);

        // Generate default translation file if directory is empty
        if (!Files.exists(languagesDir) || isEmpty(languagesDir)) {
            log.info("[YuiTranslations] No translations found, generating default en-US translation");
            generateDefaultTemplate(languagesDir);
        }

        Map<DiscordLocale, Map<String, TranslationSource>> result = new HashMap<>();

        // Load all translation files (locales) and template files
        loadFromDirectory(
                languagesDir,
                // Locale processor
                (_, locale, translations) -> {
                    Map<String, TranslationSource> sources = new HashMap<>();
                    translations.forEach((key, textValue) -> 
                        sources.put(key, new TranslationSource(textValue))
                    );
                    result.put(locale, sources);
                },
                // Template processor - templates are handled separately
                (file, templates) -> {
                    log.info("[YuiTranslations] Found {} templates in '{}'", 
                            templates.size(), file.getFileName());
                }
        );

        int totalKeys = result.values().stream().mapToInt(Map::size).sum();
        log.info("[YuiTranslations] Loaded {} translation keys across {} locales",
                totalKeys, result.size());

        return result;
    }

    public void registerTemplates(TranslationService<DiscordLocale> translationService) {
        Path languagesDir = dataPath.resolve(Constants.Structure.languagesDir);
        AtomicInteger templateCount = new AtomicInteger(0);

        loadFromDirectory(
                languagesDir,
                (_, _, _) -> {}, // Skip locales
                (_, templates) -> {
                    templates.forEach((key, textValue) -> {
                        TemplateEntry entry = new TemplateEntry(textValue.asString());
                        translationService.register(key, entry);
                        templateCount.incrementAndGet();
                    });
                }
        );

        log.info("[YuiTranslations] Registered {} template entries", templateCount.get());
    }

    private void generateDefaultTemplate(Path languagesDir) {
        try {
            Files.createDirectories(languagesDir);
            
            // MessagesTemplate has dependencies, so Configura can't instantiate it.
            // Instead, use the Spring-injected instance to populate a Messages object manually
            Messages messages = new Messages();
            messages = messagesTemplate.supply(messages);
            
            // Save the populated messages with Config.save (adds extension automatically)
            Path enUsFile = languagesDir.resolve(DiscordLocale.ENGLISH_US.getLocale());
            Config.save(enUsFile, messages);
            
            log.info("[YuiTranslations] Generated default template: {}", enUsFile);
        } catch (Exception e) {
            log.error("[YuiTranslations] Failed to generate default template", e);
        }
    }
}
