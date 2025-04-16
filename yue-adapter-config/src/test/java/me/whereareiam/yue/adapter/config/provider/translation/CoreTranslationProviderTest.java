package me.whereareiam.yue.adapter.config.provider.translation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
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
		Map<String, Map<DiscordLocale, Map<String, String>>> result = provider.loadAll();

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		assertTrue(result.containsKey(""));

		Map<DiscordLocale, Map<String, String>> localeMap = result.get("");
		assertTrue(localeMap.containsKey(DiscordLocale.ENGLISH_US));

		Map<String, String> translations = localeMap.get(DiscordLocale.ENGLISH_US);
		assertEquals("Cancel", translations.get("vocabulary.cancel"));
		assertEquals("OK", translations.get("vocabulary.ok"));
	}
}