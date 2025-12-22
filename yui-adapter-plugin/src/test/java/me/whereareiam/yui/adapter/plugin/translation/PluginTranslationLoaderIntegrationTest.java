package me.whereareiam.yui.adapter.plugin.translation;

import me.whereareiam.semantica.locale.LocaleParser;
import me.whereareiam.semantica.translation.TranslationService;
import me.whereareiam.semantica.translation.base.TranslationLocale;
import me.whereareiam.yui.localization.format.FileFormat;
import me.whereareiam.yui.localization.format.FileFormats;
import me.whereareiam.yui.localization.loader.FileTypeHandler;
import me.whereareiam.yui.localization.loader.FileTypeHandlerRegistry;
import me.whereareiam.yui.localization.provider.LocalizationProvider;
import me.whereareiam.yui.model.plugin.InternalPlugin;
import me.whereareiam.yui.model.plugin.Plugin;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class PluginTranslationLoaderIntegrationTest {

    @TempDir
    Path tempDir;

    private PluginTranslationLoader loader;
    private TranslationService<DiscordLocale> translationService;
    private LocaleParser<DiscordLocale> localeParser;
    private FileTypeHandlerRegistry handlerRegistry;

    @BeforeEach
    void setUp() {
        translationService = mock(TranslationService.class);
        localeParser = mock(LocaleParser.class);
        
        // Simple mock registry
        handlerRegistry = mock(FileTypeHandlerRegistry.class);
        when(handlerRegistry.findHandler(any(), any())).thenReturn(Optional.empty());

        loader = new PluginTranslationLoader(translationService, tempDir, localeParser, handlerRegistry);
    }

    @Test
    void loadPlugin_noFiles_generatesFromProviders() {
        InternalPlugin plugin = createMockPlugin("testplugin");
        TestLocalizationProvider provider = new TestLocalizationProvider();

        AnnotationConfigApplicationContext pluginContext = plugin.getContext();
        when(pluginContext.getBeansOfType(LocalizationProvider.class))
                .thenReturn(Map.of("testProvider", provider));

        loader.loadPlugin(plugin);

        Path languagesDir = tempDir.resolve("testplugin").resolve("languages");
        assertTrue(Files.exists(languagesDir.resolve("en-US.yml")));
    }

    @Test
    void loadPlugin_existingFiles_loadsWithPrefix() throws Exception {
        InternalPlugin plugin = createMockPlugin("testplugin");
        Path languagesDir = tempDir.resolve("testplugin").resolve("languages");
        Files.createDirectories(languagesDir);

        Path localeFile = languagesDir.resolve("en-US.yml");
        String content = """
                welcome: Welcome
                goodbye: Goodbye
                """;
        Files.writeString(localeFile, content);

        AnnotationConfigApplicationContext pluginContext = plugin.getContext();
        when(pluginContext.getBeansOfType(LocalizationProvider.class))
                .thenReturn(Map.of());

        when(localeParser.parse(any())).thenReturn(mock(TranslationLocale.class));

        // Mock handler to process the file
        FileTypeHandler mockHandler = mock(FileTypeHandler.class);
        when(handlerRegistry.findHandler(any(), any())).thenReturn(Optional.of(mockHandler));

        loader.loadPlugin(plugin);

        verify(mockHandler, atLeastOnce()).load(any(), any(), any(), any());
    }

    @Test
    void unloadPlugin_removesAllKeysWithPrefix() {
        String pluginId = "testplugin";
        String prefix = "plugin.testplugin.";

        InternalPlugin plugin = createMockPlugin(pluginId);

        when(translationService.getKeys(prefix))
                .thenReturn(Set.of(
                        "plugin.testplugin.key1",
                        "plugin.testplugin.key2",
                        "plugin.testplugin.key3"
                ));

        loader.unloadPlugin(plugin);

        verify(translationService, times(3)).unregister(startsWith(prefix));
    }

    private InternalPlugin createMockPlugin(String pluginId) {
        Plugin descriptor = mock(Plugin.class);
        when(descriptor.getId()).thenReturn(pluginId);
        when(descriptor.getName()).thenReturn(pluginId); // For simplicity, use id as name

        AnnotationConfigApplicationContext context = mock(AnnotationConfigApplicationContext.class);
        InternalPlugin plugin = mock(InternalPlugin.class);

        when(plugin.getPlugin()).thenReturn(descriptor);
        when(plugin.getContext()).thenReturn(context);

        return plugin;
    }

    // Test helper provider
    static class TestLocalizationProvider implements LocalizationProvider<TestModel> {
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
            model.put("testField", "testValue");
            return model;
        }
    }

    static class TestModel extends HashMap<String, Object> {
    }
}
