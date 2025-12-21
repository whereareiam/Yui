package me.whereareiam.yui.common.translation.loader;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.configura.Config;
import me.whereareiam.semantica.model.ProviderResult;
import me.whereareiam.semantica.model.translation.TranslationSource;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.translation.base.TranslationProvider;
import me.whereareiam.yui.Constants;
import me.whereareiam.yui.common.config.template.MessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.VocabularyTemplate;
import me.whereareiam.yui.model.config.messages.Messages;
import me.whereareiam.yui.model.config.vocabulary.Vocabulary;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class YuiTranslationLoader extends AbstractTranslationLoader implements TranslationProvider<DiscordLocale> {
    private final Path dataPath;
    private final MessagesTemplate messagesTemplate;
    private final VocabularyTemplate vocabularyTemplate;

    public YuiTranslationLoader(
            @Qualifier("dataPath") Path dataPath,
            MessagesTemplate messagesTemplate,
            VocabularyTemplate vocabularyTemplate
    ) {
        this.dataPath = dataPath;
        this.messagesTemplate = messagesTemplate;
        this.vocabularyTemplate = vocabularyTemplate;
    }

    @Override
    public ProviderResult<DiscordLocale> provide() {
        Path languagesDir = dataPath.resolve(Constants.Structure.languagesDir);

        // Generate default translation files if directory is empty
        if (!Files.exists(languagesDir) || isEmpty(languagesDir)) {
            log.info("[YuiTranslations] No translations found, generating defaults");
            generateDefaultFiles(languagesDir);
        }

        Map<DiscordLocale, Map<String, TranslationSource>> localized = new HashMap<>();
        Map<String, TemplateEntry> templates = new HashMap<>();

        // Load all translation files in a single pass
        loadFromDirectory(
                languagesDir,
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
                    log.info("[YuiTranslations] Found {} multi-locale entries in '{}'", 
                            multiLocaleData.size(), file.getFileName());
                    
                    // Convert multi-locale data to per-locale sources
                    multiLocaleData.forEach((key, localeMap) -> {
                        localeMap.forEach((locale, textValue) -> {
                            localized.computeIfAbsent(locale, _ -> new HashMap<>())
                                    .put(key, new TranslationSource(textValue));
                        });
                    });
                },
                // TEMPLATE processor
                (file, templateData) -> {
                    log.info("[YuiTranslations] Found {} templates in '{}'", 
                            templateData.size(), file.getFileName());
                    templateData.forEach((key, textValue) -> {
                        templates.put(key, new TemplateEntry(textValue.asString()));
                    });
                }
        );

        int totalKeys = localized.values().stream().mapToInt(Map::size).sum();
        log.info("[YuiTranslations] Loaded {} translation keys across {} locales",
                totalKeys, localized.size());
        log.info("[YuiTranslations] Loaded {} template entries", templates.size());

        return new ProviderResult<>(localized, templates);
    }

    private void generateDefaultFiles(Path languagesDir) {
        try {
            Files.createDirectories(languagesDir);
            
            // Generate default en-US locale file
            Messages messages = new Messages();
            messages = messagesTemplate.supply(messages);
            
            Path enUsFile = languagesDir.resolve(DiscordLocale.ENGLISH_US.getLocale());
            Config.save(enUsFile, messages);
            
            log.info("[YuiTranslations] Generated default locale file: {}", enUsFile);
            
            // Generate default vocabulary file
            Vocabulary vocabulary = new Vocabulary();
            vocabulary = vocabularyTemplate.supply(vocabulary);
            
            Path vocabularyFile = languagesDir.resolve("vocabulary");
            Config.save(vocabularyFile, vocabulary);
            
            log.info("[YuiTranslations] Generated default vocabulary file: {}", vocabularyFile);
        } catch (Exception e) {
            log.error("[YuiTranslations] Failed to generate default files", e);
        }
    }
}
