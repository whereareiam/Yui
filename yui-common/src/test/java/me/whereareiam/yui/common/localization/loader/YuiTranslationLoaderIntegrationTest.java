package me.whereareiam.yui.common.localization.loader;

import me.whereareiam.configura.Config;
import me.whereareiam.semantica.model.ProviderResult;
import me.whereareiam.yui.common.localization.format.LocaleFileHandler;
import me.whereareiam.yui.common.localization.format.MultiLocaleFileHandler;
import me.whereareiam.yui.common.localization.format.TemplateFileHandler;
import me.whereareiam.yui.config.ConfigurationTypeResolver;
import me.whereareiam.yui.localization.format.FileFormat;
import me.whereareiam.yui.localization.format.FileFormats;
import me.whereareiam.yui.localization.loader.FileTypeHandlerRegistry;
import me.whereareiam.yui.localization.provider.LocalizationProvider;
import me.whereareiam.yui.type.ConfigurationType;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.ApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class YuiTranslationLoaderIntegrationTest {
    @TempDir
    Path tempDir;

    private YuiTranslationLoader loader;
	private ApplicationContext applicationContext;

	@BeforeEach
    void setUp() {
	    FileTypeHandlerRegistry handlerRegistry = new DefaultFileTypeHandlerRegistry();
        // Register built-in handlers directly
        handlerRegistry.registerHandler(new LocaleFileHandler());
        handlerRegistry.registerHandler(new MultiLocaleFileHandler());
        handlerRegistry.registerHandler(new TemplateFileHandler());

        applicationContext = mock(ApplicationContext.class);
		ConfigurationTypeResolver configurationTypeResolver = mock(ConfigurationTypeResolver.class);
        when(configurationTypeResolver.getConfigurationType()).thenReturn(ConfigurationType.YAML);
        loader = new YuiTranslationLoader(handlerRegistry, applicationContext, configurationTypeResolver, tempDir);
    }

    @Test
    void provide_noFiles_generatesDefaultsFromProviders() {
        TestLocalizationProvider provider = new TestLocalizationProvider();
        when(applicationContext.getBeansOfType(LocalizationProvider.class))
                .thenReturn(Map.of("testProvider", provider));

        Path languagesDir = tempDir.resolve("languages");

        ProviderResult<DiscordLocale> result = loader.provide();

        assertTrue(Files.exists(languagesDir.resolve("en-US.yml")));
        assertNotNull(result);
    }

    @Test
    void provide_localeFile_loadsTranslationSources() throws Exception {
        Path languagesDir = tempDir.resolve("languages");
        Files.createDirectories(languagesDir);

        Path localeFile = languagesDir.resolve("en-US.yml");
        String content = """
                messages:
                  welcome: Welcome
                  goodbye: Goodbye
                """;
        Files.writeString(localeFile, content);

        when(applicationContext.getBeansOfType(LocalizationProvider.class))
                .thenReturn(Map.of());

        ProviderResult<DiscordLocale> result = loader.provide();

        assertNotNull(result);
        assertTrue(result.getLocalized().containsKey(DiscordLocale.ENGLISH_US));
        assertTrue(result.getLocalized().get(DiscordLocale.ENGLISH_US).containsKey("messages.welcome"));
    }

    @Test
    void provide_multiLocaleFile_distributesToLocales() throws Exception {
        Path languagesDir = tempDir.resolve("languages");
        Files.createDirectories(languagesDir);

        Path multiLocaleFile = languagesDir.resolve("vocabulary.yml");
        String content = """
                cancel:
                  en-US: Cancel
                  de: Abbrechen
                confirm:
                  en-US: Confirm
                  de: Bestätigen
                """;
        Files.writeString(multiLocaleFile, content);

        when(applicationContext.getBeansOfType(LocalizationProvider.class))
                .thenReturn(Map.of());

        ProviderResult<DiscordLocale> result = loader.provide();

        assertTrue(result.getLocalized().containsKey(DiscordLocale.ENGLISH_US));
        assertTrue(result.getLocalized().containsKey(DiscordLocale.GERMAN));
        assertTrue(result.getLocalized().get(DiscordLocale.ENGLISH_US).containsKey("vocabulary.cancel"));
        assertTrue(result.getLocalized().get(DiscordLocale.GERMAN).containsKey("vocabulary.cancel"));
    }

    @Test
    void provide_templateFile_loadsTemplateEntries() throws Exception {
        Path languagesDir = tempDir.resolve("languages");
        Files.createDirectories(languagesDir);

        Path templateFile = languagesDir.resolve("templates.yml");
        String content = """
                greeting: Hello {name}
                farewell: Goodbye {name}
                """;
        Files.writeString(templateFile, content);

        when(applicationContext.getBeansOfType(LocalizationProvider.class))
                .thenReturn(Map.of());

        ProviderResult<DiscordLocale> result = loader.provide();

        assertTrue(result.getTemplates().containsKey("greeting"));
        assertTrue(result.getTemplates().containsKey("farewell"));
    }

    @Test
    void provide_nestedLocaleFile_appliesDerivedPrefix() throws Exception {
        Path languagesDir = tempDir.resolve("languages").resolve("features").resolve("xy").resolve("messages");
        Files.createDirectories(languagesDir);

        Path localeFile = languagesDir.resolve("en-US.yml");
        Files.writeString(localeFile, "welcome: Welcome\n");

        when(applicationContext.getBeansOfType(LocalizationProvider.class))
                .thenReturn(Map.of());

        ProviderResult<DiscordLocale> result = loader.provide();

        assertTrue(result.getLocalized().containsKey(DiscordLocale.ENGLISH_US));
        assertTrue(result.getLocalized().get(DiscordLocale.ENGLISH_US)
                .containsKey("features.xy.messages.welcome"));
        assertFalse(result.getLocalized().get(DiscordLocale.ENGLISH_US).containsKey("welcome"));
    }

    @Test
    void provide_providerTargetPath_createsNestedFile() {
        LocalizationProvider<TestModel> provider = new TestLocalizationProvider() {
            @Override
            public String getTargetPath() {
                return "features/xy/messages";
            }
        };

        when(applicationContext.getBeansOfType(LocalizationProvider.class))
                .thenReturn(Map.of("provider", provider));

        loader.provide();

        Path expected = tempDir.resolve("languages")
                .resolve("features")
                .resolve("xy")
                .resolve("messages")
                .resolve("en-US.yml");

        assertTrue(Files.exists(expected));
    }

    @Test
    void provide_multipleProvidersSameTarget_applyOnceTrue_firstOneWins() {
        TestLocalizationProvider provider1 = new TestLocalizationProvider("field1", "value1");
        TestLocalizationProvider provider2 = new TestLocalizationProvider("field2", "value2");

        @SuppressWarnings("rawtypes")
        Map<String, LocalizationProvider> ordered = new LinkedHashMap<>();
        ordered.put("provider1", provider1);
        ordered.put("provider2", provider2);

        when(applicationContext.getBeansOfType(LocalizationProvider.class))
                .thenReturn(ordered);

        Path languagesDir = tempDir.resolve("languages");

        loader.provide();

        assertTrue(Files.exists(languagesDir.resolve("en-US.yml")));

        // Verify applyOnce=true providers do not overwrite existing files.
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> content = Config.load(languagesDir.resolve("en-US.yml"), Map.class);
            assertTrue(content.containsKey("field1"));
            assertFalse(content.containsKey("field2"));
        } catch (Exception e) {
            fail("Failed to load file", e);
        }
    }

    @Test
    void provide_applyOnceFalse_mergesNewKeys_preservesExistingValues() throws Exception {
        Path languagesDir = tempDir.resolve("languages");
        Files.createDirectories(languagesDir);

        // Existing file with user-modified value
        Files.writeString(languagesDir.resolve("en-US.yml"), "field1: custom\n");

        LocalizationProvider<TestModel> provider = new TestLocalizationProvider("field2", "value2") {
            @Override
            public TestModel supply(TestModel model) {
                model.put("field1", "default");
                model.put("field2", "value2");
                return model;
            }

            @Override
            public boolean applyOnce() {
                return false;
            }
        };

        when(applicationContext.getBeansOfType(LocalizationProvider.class))
                .thenReturn(Map.of("provider", provider));

        loader.provide();

        @SuppressWarnings("unchecked")
        Map<String, Object> content = Config.load(languagesDir.resolve("en-US.yml"), Map.class);
        assertEquals("custom", content.get("field1"));
        assertEquals("value2", content.get("field2"));
    }

    // Test helper provider
    static class TestLocalizationProvider implements LocalizationProvider<TestModel> {
        private final String fieldName;
        private final String fieldValue;

        TestLocalizationProvider() {
            this("testField", "testValue");
        }

        TestLocalizationProvider(String fieldName, String fieldValue) {
            this.fieldName = fieldName;
            this.fieldValue = fieldValue;
        }

        @Override
        public Class<TestModel> getModelClass() {
            return TestModel.class;
        }

        @Override
        public FileFormat getFormat() {
            return FileFormats.LOCALE;
        }

        @Override
        public TestModel supply(TestModel model) {
            model.put(fieldName, fieldValue);
            return model;
        }
    }

    static class TestModel extends HashMap<String, Object> {
    }
}
