package me.whereareiam.yui.adapter.database;

import me.whereareiam.yui.adapter.database.adapter.LanguageServiceAdapter;
import me.whereareiam.yui.adapter.database.entity.LanguageEntity;
import me.whereareiam.yui.adapter.database.repository.LanguageRepository;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
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
		DiscordLocale locale = DiscordLocale.ENGLISH_US;
		when(languageRepository.findByLocale(locale)).thenReturn(Optional.empty());

		languageService.addLanguage(locale);

		verify(languageRepository).save(any(LanguageEntity.class));
	}

	@Test
	void addLanguage_whenLanguageExists_shouldNotSaveLanguage() {
		DiscordLocale locale = DiscordLocale.ENGLISH_US;
		when(languageRepository.findByLocale(locale)).thenReturn(Optional.of(LanguageEntity.builder().locale(locale).build()));

		languageService.addLanguage(locale);

		verify(languageRepository, never()).save(any(LanguageEntity.class));
	}

	@Test
	void removeLanguage_shouldDeleteLanguageByLocale() {
		DiscordLocale locale = DiscordLocale.ENGLISH_US;

		languageService.removeLanguage(locale);

		verify(languageRepository).deleteByLocale(locale);
	}

	@Test
	void languageExists_whenLanguageExists_shouldReturnTrue() {
		DiscordLocale locale = DiscordLocale.ENGLISH_US;
		when(languageRepository.findByLocale(locale)).thenReturn(Optional.of(LanguageEntity.builder().locale(locale).build()));

		boolean result = languageService.languageExists(locale);

		assertTrue(result);
	}

	@Test
	void languageExists_whenLanguageDoesNotExist_shouldReturnFalse() {
		DiscordLocale locale = DiscordLocale.ENGLISH_US;
		when(languageRepository.findByLocale(locale)).thenReturn(Optional.empty());

		boolean result = languageService.languageExists(locale);

		assertFalse(result);
	}

	@Test
	void getAvailableLanguages_shouldReturnAllLanguages() {
		LanguageEntity en = LanguageEntity.builder().locale(DiscordLocale.ENGLISH_US).build();
		LanguageEntity fr = LanguageEntity.builder().locale(DiscordLocale.FRENCH).build();
		when(languageRepository.findAll()).thenReturn(Arrays.asList(en, fr));

		List<DiscordLocale> result = languageService.getAvailableLanguages();

		assertEquals(2, result.size());
		assertTrue(result.contains(DiscordLocale.ENGLISH_US));
		assertTrue(result.contains(DiscordLocale.FRENCH));
	}
}
