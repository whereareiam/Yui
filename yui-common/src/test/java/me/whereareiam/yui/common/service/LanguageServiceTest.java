package me.whereareiam.yui.common.service;

import me.whereareiam.yui.Registry;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yui.common.config.provider.LanguagesProvider;
import me.whereareiam.yui.model.config.languages.LanguageEntry;
import me.whereareiam.yui.model.config.languages.Languages;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.persistence.LanguagePersistence;
import me.whereareiam.yui.service.RoleService;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LanguageServiceTest {
	@Mock
	private LanguagesProvider languagesProvider;
	
	@Mock
	private LanguagePersistence languagePersistence;
	
	@Mock
	private RoleService roleService;
	
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
				languagesProvider, languagePersistence, roleService, settingsProvider, reloadableRegistry
		);
		
		when(settingsProvider.getObject()).thenReturn(settings);
		when(settings.getLocale()).thenReturn(DiscordLocale.ENGLISH_US);
	}
	
	@Test
	void testSyncLanguagesWithDatabase_AddsEnabledLanguagesFromConfig() {
		// Arrange
		LanguageEntry enEntry = new LanguageEntry();
		enEntry.setEnabled(true);
		
		LanguageEntry deEntry = new LanguageEntry();
		deEntry.setEnabled(true);
		
		Languages config = new Languages();
		Map<String, LanguageEntry> languagesMap = new HashMap<>();
		languagesMap.put("en-US", enEntry);
		languagesMap.put("de", deEntry);
		config.setLanguages(languagesMap);
		
		when(languagesProvider.get()).thenReturn(config);
		when(languagePersistence.languageExists(DiscordLocale.ENGLISH_US)).thenReturn(false);
		when(languagePersistence.languageExists(DiscordLocale.GERMAN)).thenReturn(false);
		when(languagePersistence.getAvailableLanguages()).thenReturn(new ArrayList<>());
		
		// Act
		languageService.syncLanguagesWithDatabase();
		
		// Assert
		verify(languagePersistence).addLanguage(DiscordLocale.ENGLISH_US);
		verify(languagePersistence).addLanguage(DiscordLocale.GERMAN);
	}
	
	@Test
	void testSyncLanguagesWithDatabase_RemovesLanguagesNotInConfig() {
		// Arrange
		Languages config = new Languages();
		Map<String, LanguageEntry> languagesMap = new HashMap<>();
		languagesMap.put("en-US", new LanguageEntry());
		config.setLanguages(languagesMap);
		
		when(languagesProvider.get()).thenReturn(config);
		when(languagePersistence.languageExists(DiscordLocale.ENGLISH_US)).thenReturn(true);
		when(languagePersistence.getAvailableLanguages()).thenReturn(
				Arrays.asList(DiscordLocale.ENGLISH_US, DiscordLocale.GERMAN, DiscordLocale.FRENCH)
		);
		
		// Act
		languageService.syncLanguagesWithDatabase();
		
		// Assert
		verify(languagePersistence, never()).addLanguage(DiscordLocale.ENGLISH_US); // Already exists
		verify(languagePersistence).removeLanguage(DiscordLocale.GERMAN);
		verify(languagePersistence).removeLanguage(DiscordLocale.FRENCH);
	}
	
	@Test
	void testSyncLanguagesWithDatabase_FiltersOutUnknownLocales() {
		// Arrange
		LanguageEntry unknownEntry = new LanguageEntry();
		unknownEntry.setEnabled(true);
		
		Languages config = new Languages();
		Map<String, LanguageEntry> languagesMap = new HashMap<>();
		languagesMap.put("UNKNOWN", unknownEntry); // Invalid locale string
		languagesMap.put("en-US", new LanguageEntry());
		config.setLanguages(languagesMap);
		
		when(languagesProvider.get()).thenReturn(config);
		when(languagePersistence.languageExists(DiscordLocale.ENGLISH_US)).thenReturn(false);
		when(languagePersistence.getAvailableLanguages()).thenReturn(new ArrayList<>());
		
		// Act
		languageService.syncLanguagesWithDatabase();
		
		// Assert
		// Should only add ENGLISH_US, not UNKNOWN
		verify(languagePersistence).addLanguage(DiscordLocale.ENGLISH_US);
		verify(languagePersistence, never()).addLanguage(DiscordLocale.UNKNOWN);
	}
}

