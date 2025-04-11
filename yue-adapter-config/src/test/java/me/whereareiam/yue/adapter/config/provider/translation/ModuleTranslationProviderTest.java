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
class ModuleTranslationProviderTest {
	@Mock
	private ObjectMapper objectMapper;

	private ModuleTranslationProvider provider;

	@TempDir
	Path tempDir;

	@BeforeEach
	void setUp() throws Exception {
		// Create module directory structure
		Path musicModule = tempDir.resolve("music");
		Files.createDirectory(musicModule);

		Path langDir = musicModule.resolve("languages");
		Files.createDirectory(langDir);

		Path enFile = langDir.resolve("en.json");
		Files.writeString(enFile, "{}");

		// Setup mock behavior
		Map<String, Object> moduleTranslations = Map.of(
				"vocabulary", Map.of(
						"play", "Play",
						"stop", "Stop"
				)
		);
		when(objectMapper.readValue(any(File.class), any(TypeReference.class)))
				.thenReturn(moduleTranslations);

		provider = new ModuleTranslationProvider(tempDir, objectMapper);
	}

	@Test
	void loadAll_shouldLoadModuleTranslations() {
		// Act
		Map<String, Map<Locale, Map<String, String>>> result = provider.loadAll();

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		assertTrue(result.containsKey("module.music."));

		Map<Locale, Map<String, String>> localeMap = result.get("module.music.");
		assertTrue(localeMap.containsKey(Locale.ENGLISH));

		Map<String, String> translations = localeMap.get(Locale.ENGLISH);
		assertEquals("Play", translations.get("vocabulary.play"));
		assertEquals("Stop", translations.get("vocabulary.stop"));
	}
}