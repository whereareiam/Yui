package me.whereareiam.yui.common.component;

import me.whereareiam.semantica.translation.TranslationService;
import me.whereareiam.yui.fluctlight.FluctlightService;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.util.translation.Translatable;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.ObjectProvider;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TranslatableTest {

    @Mock
    private TranslationService<DiscordLocale> translationService;

    @Mock
    private FluctlightService fluctlightService;

    @Mock
    private ObjectProvider<Settings> settingsProvider;

    @Mock
    private Settings settings;

    @BeforeEach
    void setUp() throws Exception {
        // Setup default settings mock
        when(settingsProvider.getObject()).thenReturn(settings);
        when(settings.getLocale()).thenReturn(DiscordLocale.ENGLISH_US);

        // Use reflection to set static fields
        setField("service", translationService);
        setField("fluctlightService", fluctlightService);
        setField("settingsProvider", settingsProvider);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Reset all static fields
        setField("service", null);
        setField("fluctlightService", null);
        setField("settingsProvider", null);
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = Translatable.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }

    @Test
    void of_withKeyOnly_shouldUseDefaultLocale() {
        // Arrange
        when(translationService.resolve("vocabulary.test", DiscordLocale.ENGLISH_US))
                .thenReturn("Test Translation");

        // Act
        String result = Translatable.text("vocabulary.test").resolveDefault();

        // Assert
        assertEquals("Test Translation", result);
        verify(translationService).resolve("vocabulary.test", DiscordLocale.ENGLISH_US);
    }

    @Test
    void of_withUserId_shouldResolveUserLocale() {
        // Arrange
        Fluctlight fluctlight = mock(Fluctlight.class);
        when(fluctlight.getPrimaryLanguage()).thenReturn(DiscordLocale.GERMAN);
        when(fluctlightService.get(123L)).thenReturn(Optional.of(fluctlight));
        when(translationService.resolve("vocabulary.test", DiscordLocale.GERMAN))
                .thenReturn("German Translation");

        // Act
        String result = Translatable.text("vocabulary.test").resolve(123L);

        // Assert
        assertEquals("German Translation", result);
        verify(translationService).resolve("vocabulary.test", DiscordLocale.GERMAN);
    }

    @Test
    void of_withUserIdNoFluctlight_shouldFallbackToDefaultLocale() {
        // Arrange
        when(fluctlightService.get(999L)).thenReturn(Optional.empty());
        when(translationService.resolve("vocabulary.test", DiscordLocale.ENGLISH_US))
                .thenReturn("Default Translation");

        // Act
        String result = Translatable.text("vocabulary.test").resolve(999L);

        // Assert
        assertEquals("Default Translation", result);
        verify(translationService).resolve("vocabulary.test", DiscordLocale.ENGLISH_US);
    }

    @Test
    void of_withFluctlight_shouldUseFluctlightLocale() {
        // Arrange
        Fluctlight fluctlight = mock(Fluctlight.class);
        when(fluctlight.getId()).thenReturn(123L);
        when(fluctlight.getPrimaryLanguage()).thenReturn(DiscordLocale.FRENCH);
        when(fluctlightService.get(123L)).thenReturn(Optional.of(fluctlight));
        when(translationService.resolve("vocabulary.test", DiscordLocale.FRENCH))
                .thenReturn("French Translation");

        // Act
        String result = Translatable.text("vocabulary.test").resolve(fluctlight);

        // Assert
        assertEquals("French Translation", result);
        verify(translationService).resolve("vocabulary.test", DiscordLocale.FRENCH);
    }

    @Test
    void of_withLocale_shouldUseSpecifiedLocale() {
        // Arrange
        when(translationService.resolve("vocabulary.test", DiscordLocale.JAPANESE))
                .thenReturn("Japanese Translation");

        // Act
        String result = Translatable.text("vocabulary.test").resolve(DiscordLocale.JAPANESE);

        // Assert
        assertEquals("Japanese Translation", result);
        verify(translationService).resolve("vocabulary.test", DiscordLocale.JAPANESE);
    }

    @Test
    void text_withNamedPlaceholders_shouldResolveCorrectly() {
        // Arrange
        when(translationService.resolve(eq("greeting.welcome"), eq(DiscordLocale.ENGLISH_US), any()))
                .thenReturn("Hello Alice, you have 5 messages");

        // Act
        String result = Translatable.text("greeting.welcome")
                .with("name", "Alice")
                .with("count", 5)
                .resolveDefault();

        // Assert
        assertEquals("Hello Alice, you have 5 messages", result);
        verify(translationService).resolve(eq("greeting.welcome"), eq(DiscordLocale.ENGLISH_US), argThat(map ->
                map.get("name").equals("Alice") && map.get("count").equals(5)
        ));
    }

    @Test
    void text_withFluctlight_shouldUseFluctlightLocale() {
        // Arrange
        Fluctlight fluctlight = mock(Fluctlight.class);
        when(fluctlight.getId()).thenReturn(123L);
        when(fluctlight.getPrimaryLanguage()).thenReturn(DiscordLocale.GERMAN);
        when(fluctlightService.get(123L)).thenReturn(Optional.of(fluctlight));
        when(translationService.resolve(eq("test.key"), eq(DiscordLocale.GERMAN), any()))
                .thenReturn("German text");

        // Act
        String result = Translatable.text("test.key")
                .with("param", "value")
                .resolve(fluctlight);

        // Assert
        assertEquals("German text", result);
        verify(translationService).resolve(eq("test.key"), eq(DiscordLocale.GERMAN), any(Map.class));
    }

    @Test
    void text_withMultiplePlaceholders_shouldChainCorrectly() {
        // Arrange
        when(translationService.resolve(eq("complex.message"), eq(DiscordLocale.ENGLISH_US), any()))
                .thenReturn("Player Alice has 100 points and 5 items");

        // Act
        String result = Translatable.text("complex.message")
                .with("playerName", "Alice")
                .with("points", 100)
                .with("items", 5)
                .resolveDefault();

        // Assert
        assertEquals("Player Alice has 100 points and 5 items", result);
    }

    @Test
    void text_withMapPlaceholders_shouldResolveCorrectly() {
        // Arrange
        Map<String, Object> placeholders = Map.of(
                "name", "Bob",
                "level", 42
        );
        when(translationService.resolve(eq("level.up"), eq(DiscordLocale.ENGLISH_US), any()))
                .thenReturn("Bob reached level 42");

        // Act
        String result = Translatable.text("level.up")
                .with(placeholders)
                .resolveDefault();

        // Assert
        assertEquals("Bob reached level 42", result);
    }

    @Test
    void with_shouldBeImmutable_notMutateOriginal() {
        // Arrange
        when(translationService.resolve(eq("greeting"), eq(DiscordLocale.ENGLISH_US), any()))
                .thenAnswer(invocation -> {
                    Map<String, Object> placeholders = invocation.getArgument(2);
                    return "Hello " + placeholders.get("name");
                });

        // Act
        Translatable base = Translatable.text("greeting");
        Translatable withAlice = base.with("name", "Alice");
        Translatable withBob = base.with("name", "Bob");

        String result1 = withAlice.resolveDefault();
        String result2 = withBob.resolveDefault();
        
        // Should not affect each other
        String result3 = withAlice.resolveDefault();

        // Assert
        assertEquals("Hello Alice", result1);
        assertEquals("Hello Bob", result2);
        assertEquals("Hello Alice", result3); // Should still be Alice, not Bob
    }

    @Test
    void with_shouldCreateNewInstance_notMutateExisting() {
        // Arrange
        Translatable base = Translatable.text("test");
        Translatable withParam1 = base.with("param1", "value1");
        Translatable withParam2 = withParam1.with("param2", "value2");

        when(translationService.resolve(eq("test"), eq(DiscordLocale.ENGLISH_US), any()))
                .thenAnswer(invocation -> {
                    Map<String, Object> placeholders = invocation.getArgument(2);
                    return "Size: " + placeholders.size();
                });

        // Act & Assert
        assertEquals("Size: 1", withParam1.resolveDefault()); // Only param1
        assertEquals("Size: 2", withParam2.resolveDefault()); // param1 + param2
    }

    @Test
    void of_whenServiceNull_shouldReturnOriginalKey() throws Exception {
        // Arrange
        setField("service", null);

        // Act
        String result = Translatable.text("vocabulary.test").resolve(123L);

        // Assert
        assertEquals("vocabulary.test", result);
    }

    @Test
    void of_withUserIdNoPrimaryLanguage_shouldFallbackToDefaultLocale() {
        // Arrange
        Fluctlight fluctlight = mock(Fluctlight.class);
        when(fluctlight.getPrimaryLanguage()).thenReturn(null);
        when(fluctlightService.get(123L)).thenReturn(Optional.of(fluctlight));
        when(translationService.resolve("vocabulary.test", DiscordLocale.ENGLISH_US))
                .thenReturn("Default Translation");

        // Act
        String result = Translatable.text("vocabulary.test").resolve(123L);

        // Assert
        assertEquals("Default Translation", result);
        verify(translationService).resolve("vocabulary.test", DiscordLocale.ENGLISH_US);
    }
}
