package me.whereareiam.yui.common.adapter;

import me.whereareiam.yui.registry.Registry;
import me.whereareiam.yui.translation.TranslationLoader;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.profile.UserProfile;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yui.Provider;
import me.whereareiam.yui.registry.UserProfileCacheRegistry;
import me.whereareiam.yui.common.service.DefaultTranslationService;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
	private UserProfileCacheRegistry userProfileCache;

	@Mock
	private Registry<Reloadable> reloadableRegistry;

	@Mock
	private Provider<Settings> settings;

	private DefaultTranslationService translationService;

	@BeforeEach
	void setUp() {
		Settings botSettings = mock(Settings.class);
		when(botSettings.getLocale()).thenReturn(DiscordLocale.ENGLISH_US);
		when(settings.get()).thenReturn(botSettings);

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
				userProfileCache,
				reloadableRegistry
		);

		translationService.initialize();
	}

	@Test
	void translate_withExistingKey_shouldReturnTranslation() {
		// Arrange
		UserProfile profile = new UserProfile(123, DiscordLocale.ENGLISH_US);
		when(userProfileCache.getProfile(123L)).thenReturn(Optional.of(profile));

		// Act & Assert
		assertEquals("Cancel", translationService.translate("vocabulary.cancel", 123L));
		assertEquals("Play", translationService.translate("plugin.music.vocabulary.play", 123L));
	}

	@Test
	void translate_withUnknownUser_shouldUseDefaultLocale() {
		// Arrange
		when(userProfileCache.getProfile(999L)).thenReturn(Optional.empty());

		// Act & Assert
		assertEquals("Cancel", translationService.translate("vocabulary.cancel", 999L));
	}

	@Test
	void translate_withLocaleFallback_shouldSelectCorrectTranslation() {
		// Arrange
		UserProfile profile = new UserProfile(456, DiscordLocale.GERMAN);
		when(userProfileCache.getProfile(456L)).thenReturn(Optional.of(profile));

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