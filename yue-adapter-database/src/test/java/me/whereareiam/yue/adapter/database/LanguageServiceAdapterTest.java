package me.whereareiam.yue.adapter.database;

import me.whereareiam.yue.adapter.database.adapter.LanguageServiceAdapter;
import me.whereareiam.yue.adapter.database.entity.LanguageEntity;
import me.whereareiam.yue.adapter.database.repository.LanguageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LanguageServiceAdapterTest {

	@Mock
	private LanguageRepository languageRepository;

	private LanguageServiceAdapter languageService;

	@BeforeEach
	void setUp() {
		languageService = new LanguageServiceAdapter(languageRepository);
	}

	@Test
	void addLanguage_whenLanguageDoesNotExist_shouldSaveLanguage() {
		Locale locale = Locale.ENGLISH;
		when(languageRepository.findByLocale(locale)).thenReturn(Optional.empty());

		languageService.addLanguage(locale);

		verify(languageRepository).save(any(LanguageEntity.class));
	}

	@Test
	void addLanguage_whenLanguageExists_shouldNotSaveLanguage() {
		Locale locale = Locale.ENGLISH;
		when(languageRepository.findByLocale(locale)).thenReturn(Optional.of(LanguageEntity.builder().locale(locale).build()));

		languageService.addLanguage(locale);

		verify(languageRepository, never()).save(any(LanguageEntity.class));
	}

	@Test
	void removeLanguage_shouldDeleteLanguageByLocale() {
		Locale locale = Locale.ENGLISH;

		languageService.removeLanguage(locale);

		verify(languageRepository).deleteByLocale(locale);
	}

	@Test
	void languageExists_whenLanguageExists_shouldReturnTrue() {
		Locale locale = Locale.ENGLISH;
		when(languageRepository.findByLocale(locale)).thenReturn(Optional.of(LanguageEntity.builder().locale(locale).build()));

		boolean result = languageService.languageExists(locale);

		assertTrue(result);
	}

	@Test
	void languageExists_whenLanguageDoesNotExist_shouldReturnFalse() {
		Locale locale = Locale.ENGLISH;
		when(languageRepository.findByLocale(locale)).thenReturn(Optional.empty());

		boolean result = languageService.languageExists(locale);

		assertFalse(result);
	}

	@Test
	void getAvailableLanguages_shouldReturnAllLanguages() {
		LanguageEntity en = LanguageEntity.builder().locale(Locale.ENGLISH).build();
		LanguageEntity fr = LanguageEntity.builder().locale(Locale.FRENCH).build();
		when(languageRepository.findAll()).thenReturn(Arrays.asList(en, fr));

		List<Locale> result = languageService.getAvailableLanguages();

		assertEquals(2, result.size());
		assertTrue(result.contains(Locale.ENGLISH));
		assertTrue(result.contains(Locale.FRENCH));
	}
}
