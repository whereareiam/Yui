package me.whereareiam.yui.common.adapter;

import me.whereareiam.yui.Registry;
import me.whereareiam.yui.translation.TranslationLoader;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yui.fluctlight.FluctlightService;
import me.whereareiam.yui.common.translation.DefaultTranslationService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultTranslationServiceTest {
	@Mock
	private TranslationLoader coreLoader;

	@Mock
	private TranslationLoader pluginLoader;

	@Mock
	private FluctlightService fluctlightService;

	@Mock
	private Registry<Reloadable> reloadableRegistry;

	@Mock
	private ObjectProvider<Settings> settings;

	@Mock
	private User jdaUser;

	private DefaultTranslationService translationService;

	@BeforeEach
	void setUp() {
		Settings botSettings = mock(Settings.class);
		when(botSettings.getLocale()).thenReturn(DiscordLocale.ENGLISH_US);
		when(settings.getObject()).thenReturn(botSettings);

		// Core translations
		Map<String, Map<DiscordLocale, Map<String, String>>> coreTranslations = new HashMap<>();
		Map<DiscordLocale, Map<String, String>> coreLocaleMap = new HashMap<>();

		Map<String, String> enCore = new HashMap<>();
		enCore.put("vocabulary.cancel", "Cancel");
		enCore.put("vocabulary.ok", "OK");

		Map<String, String> deCore = new HashMap<>();
		deCore.put("vocabulary.cancel", "Abbrechen");
		deCore.put("vocabulary.ok", "OK");

		coreLocaleMap.put(DiscordLocale.ENGLISH_US, enCore);
		coreLocaleMap.put(DiscordLocale.GERMAN, deCore);
		coreTranslations.put("", coreLocaleMap);

		// Plugin translations
		Map<String, Map<DiscordLocale, Map<String, String>>> pluginTranslations = new HashMap<>();
		Map<DiscordLocale, Map<String, String>> pluginLocaleMap = new HashMap<>();

		Map<String, String> enPlugin = new HashMap<>();
		enPlugin.put("vocabulary.play", "Play");

		Map<String, String> dePlugin = new HashMap<>();
		dePlugin.put("vocabulary.play", "Abspielen");

		pluginLocaleMap.put(DiscordLocale.ENGLISH_US, enPlugin);
		pluginLocaleMap.put(DiscordLocale.GERMAN, dePlugin);
		pluginTranslations.put("plugin.music.", pluginLocaleMap);

		when(coreLoader.load()).thenReturn(coreTranslations);
		when(pluginLoader.load()).thenReturn(pluginTranslations);

		translationService = new DefaultTranslationService(
				settings,
				List.of(coreLoader, pluginLoader),
				fluctlightService,
				reloadableRegistry
		);

		translationService.initialize();
	}

	@Test
	void translate_withExistingKey_shouldReturnTranslation() {
		// Arrange
		when(jdaUser.getIdLong()).thenReturn(123L);
		Fluctlight fluctlight = new Fluctlight(jdaUser);
		fluctlight.setPrimaryLanguage(DiscordLocale.ENGLISH_US);
		when(fluctlightService.get(123L)).thenReturn(Optional.of(fluctlight));

		// Act & Assert
		assertEquals("Cancel", translationService.translate("vocabulary.cancel", 123L));
		assertEquals("Play", translationService.translate("plugin.music.vocabulary.play", 123L));
	}

	@Test
	void translate_withUnknownUser_shouldUseDefaultLocale() {
		// Arrange
		when(fluctlightService.get(999L)).thenReturn(Optional.empty());

		// Act & Assert
		assertEquals("Cancel", translationService.translate("vocabulary.cancel", 999L));
	}

	@Test
	void translate_withLocaleFallback_shouldSelectCorrectTranslation() {
		// Arrange
		when(jdaUser.getIdLong()).thenReturn(456L);
		Fluctlight fluctlight = new Fluctlight(jdaUser);
		fluctlight.setPrimaryLanguage(DiscordLocale.GERMAN);
		when(fluctlightService.get(456L)).thenReturn(Optional.of(fluctlight));

		// Act & Assert
		assertEquals("Abbrechen", translationService.translate("vocabulary.cancel", 456L));
		assertEquals("Abspielen", translationService.translate("plugin.music.vocabulary.play", 456L));
	}

	@Test
	void translate_withMissingKey_shouldReturnOriginalKey() {
		// Act & Assert
		assertEquals("vocabulary.missing", translationService.translate("vocabulary.missing", 0L));
	}
}
