package me.whereareiam.yui.common.service;

import me.whereareiam.yui.api.Registry;
import me.whereareiam.yui.api.Reloadable;
import me.whereareiam.yui.api.model.config.languages.Language;
import me.whereareiam.yui.api.model.config.languages.Languages;
import me.whereareiam.yui.api.model.config.settings.Settings;
import me.whereareiam.yui.api.persistence.LanguagePersistence;
import me.whereareiam.yui.common.config.provider.LanguagesProvider;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LanguageServiceTest {
	@Mock
	private LanguagesProvider languagesProvider;
	
	@Mock
	private LanguagePersistence languagePersistence;
	
	
	@Mock
	private ObjectProvider<Settings> settingsProvider;
	
	@Mock
	private Settings settings;
	
	@Mock
	private Registry<Reloadable> reloadableRegistry;
	
	private LanguageService languageService;
	
	@BeforeEach
	void setUp() {
		languageService = new LanguageService(
				languagesProvider, languagePersistence, settingsProvider, reloadableRegistry
		);
		
		when(settingsProvider.getObject()).thenReturn(settings);
		when(settings.getLocale()).thenReturn(DiscordLocale.ENGLISH_US);
	}
	
	@Test
	void sync_AddsTheListedLanguages() {
		when(languagesProvider.get()).thenReturn(languages(DiscordLocale.ENGLISH_US, DiscordLocale.GERMAN));
		when(languagePersistence.getAvailableLanguages()).thenReturn(new ArrayList<>());

		languageService.syncLanguagesWithDatabase();

		verify(languagePersistence).addLanguage(DiscordLocale.ENGLISH_US);
		verify(languagePersistence).addLanguage(DiscordLocale.GERMAN);
	}

	@Test
	void sync_RemovesLanguagesNoLongerListed() {
		when(languagesProvider.get()).thenReturn(languages(DiscordLocale.ENGLISH_US));
		when(languagePersistence.languageExists(DiscordLocale.ENGLISH_US)).thenReturn(true);
		when(languagePersistence.getAvailableLanguages()).thenReturn(
				Arrays.asList(DiscordLocale.ENGLISH_US, DiscordLocale.GERMAN, DiscordLocale.FRENCH)
		);

		languageService.syncLanguagesWithDatabase();

		verify(languagePersistence, never()).addLanguage(DiscordLocale.ENGLISH_US);
		verify(languagePersistence).removeLanguage(DiscordLocale.GERMAN);
		verify(languagePersistence).removeLanguage(DiscordLocale.FRENCH);
	}

	@Test
	void sync_OffersTheDefaultLocaleEvenWhenItIsNotListed() {
		when(languagesProvider.get()).thenReturn(languages(DiscordLocale.GERMAN));
		when(languagePersistence.getAvailableLanguages()).thenReturn(new ArrayList<>());

		languageService.syncLanguagesWithDatabase();

		verify(languagePersistence).addLanguage(DiscordLocale.GERMAN);
		verify(languagePersistence).addLanguage(DiscordLocale.ENGLISH_US);
	}

	private static Languages languages(DiscordLocale... locales) {
		Map<DiscordLocale, Language> languages = new LinkedHashMap<>();
		for (DiscordLocale locale : locales) languages.put(locale, new Language());

		return new Languages(languages);
	}
}
