package me.whereareiam.yue.adapter.config.provider.translation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreTranslationProviderTest {
	@Mock
	private ObjectMapper objectMapper;

	private CoreTranslationProvider provider;

	@TempDir
	Path tempDir;

	@BeforeEach
	void setUp() throws Exception {
		// Create languages directory
		Path langDir = tempDir.resolve("languages");
		Files.createDirectory(langDir);

		// Create sample translation files
		Path enFile = langDir.resolve("en.json");
		Files.writeString(enFile, "{}");

		// Setup mock behavior
		Map<String, Object> enTranslations = Map.of(
				"vocabulary", Map.of(
						"cancel", "Cancel",
						"ok", "OK"
				)
		);
		when(objectMapper.readValue(any(File.class), any(TypeReference.class)))
				.thenReturn(enTranslations);

		provider = new CoreTranslationProvider(tempDir, objectMapper);
	}

	@Test
	void loadAll_shouldLoadCoreTranslations() {
		// Act
		Map<String, Map<Locale, Map<String, String>>> result = provider.loadAll();

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		assertTrue(result.containsKey(""));

		Map<Locale, Map<String, String>> localeMap = result.get("");
		assertTrue(localeMap.containsKey(Locale.ENGLISH));

		Map<String, String> translations = localeMap.get(Locale.ENGLISH);
		assertEquals("Cancel", translations.get("vocabulary.cancel"));
		assertEquals("OK", translations.get("vocabulary.ok"));
	}
}