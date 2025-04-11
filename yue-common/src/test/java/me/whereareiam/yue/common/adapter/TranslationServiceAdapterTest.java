package me.whereareiam.yue.common.adapter;

import me.whereareiam.yue.api.input.translation.TranslationLoader;
import me.whereareiam.yue.api.model.config.settings.Settings;
import me.whereareiam.yue.api.model.profile.UserProfile;
import me.whereareiam.yue.api.output.provider.UserProfileCacheProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranslationServiceAdapterTest {
	@Mock
	private TranslationLoader coreLoader;

	@Mock
	private TranslationLoader moduleLoader;

	@Mock
	private UserProfileCacheProvider userProfileCache;

	@Mock
	private Settings settings;

	private TranslationServiceAdapter translationService;

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

		// Module translations
		Map<String, Map<Locale, Map<String, String>>> moduleTranslations = new HashMap<>();
		Map<Locale, Map<String, String>> moduleLocaleMap = new HashMap<>();

		Map<String, String> enModule = new HashMap<>();
		enModule.put("vocabulary.play", "Play");

		Map<String, String> deModule = new HashMap<>();
		deModule.put("vocabulary.play", "Abspielen");

		moduleLocaleMap.put(Locale.ENGLISH, enModule);
		moduleLocaleMap.put(Locale.GERMAN, deModule);
		moduleTranslations.put("module.music.", moduleLocaleMap);

		when(coreLoader.loadAll()).thenReturn(coreTranslations);
		when(moduleLoader.loadAll()).thenReturn(moduleTranslations);
		when(settings.getLocale()).thenReturn(Locale.ENGLISH);

		translationService = new TranslationServiceAdapter(
				List.of(coreLoader, moduleLoader),
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
		assertEquals("Play", translationService.translate("module.music.vocabulary.play", 123L));
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
		assertEquals("Abspielen", translationService.translate("module.music.vocabulary.play", 456L));
	}

	@Test
	void translate_withMissingKey_shouldReturnOriginalKey() {
		// Act & Assert
		assertEquals("vocabulary.missing", translationService.translate("vocabulary.missing", 0L));
	}
}