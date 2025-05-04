package me.whereareiam.yui.common.adapter;

import me.whereareiam.yui.api.input.translation.TranslationLoader;
import me.whereareiam.yui.api.model.config.settings.Settings;
import me.whereareiam.yui.api.model.profile.UserProfile;
import me.whereareiam.yui.api.output.provider.UserProfileCacheProvider;
import me.whereareiam.yui.common.service.DefaultTranslationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultTranslationServiceTest {
	@Mock
	private TranslationLoader coreLoader;

	@Mock
	private TranslationLoader pluginLoader;

	@Mock
	private UserProfileCacheProvider userProfileCache;

	@Mock
	private Settings settings;

	private DefaultTranslationService translationService;

	@BeforeEach
	void setUp() {
		// Core translations
		Map<String, Map<Locale, Map<String, String>>> coreTranslations = new HashMap<>();
		Map<Locale, Map<String, String>> coreLocaleMap = new HashMap<>();

		Map<String, String> enCore = new HashMap<>();
		enCore.put("vocabulary.cancel", "Cancel");
		enCore.put("vocabulary.ok", "OK");

		Map<String, String> deCore = new HashMap<>();
		deCore.put("vocabulary.cancel", "Abbrechen");
		deCore.put("vocabulary.ok", "OK");

		coreLocaleMap.put(Locale.ENGLISH, enCore);
		coreLocaleMap.put(Locale.GERMAN, deCore);
		coreTranslations.put("", coreLocaleMap);

		// Plugin translations
		Map<String, Map<Locale, Map<String, String>>> pluginTranslations = new HashMap<>();
		Map<Locale, Map<String, String>> pluginLocaleMap = new HashMap<>();

		Map<String, String> enPlugin = new HashMap<>();
		enPlugin.put("vocabulary.play", "Play");

		Map<String, String> dePlugin = new HashMap<>();
		dePlugin.put("vocabulary.play", "Abspielen");

		pluginLocaleMap.put(Locale.ENGLISH, enPlugin);
		pluginLocaleMap.put(Locale.GERMAN, dePlugin);
		pluginTranslations.put("plugin.music.", pluginLocaleMap);

		when(coreLoader.loadAll()).thenReturn(coreTranslations);
		when(pluginLoader.loadAll()).thenReturn(pluginTranslations);
		when(settings.getLocale()).thenReturn(Locale.ENGLISH);

		translationService = new DefaultTranslationService(
				List.of(coreLoader, pluginLoader),
				userProfileCache,
				settings
		);

		translationService.init();
	}

	@Test
	void translate_withExistingKey_shouldReturnTranslation() {
		// Arrange
		UserProfile profile = new UserProfile(123, Locale.ENGLISH);
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
		UserProfile profile = new UserProfile(456, Locale.GERMAN);
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