package me.whereareiam.yui.common.translation;

import me.whereareiam.configura.Config;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CoreTranslationProviderTest {
	private CoreTranslationProvider provider;

	@TempDir
	Path tempDir;

	@BeforeEach
	void setUp() throws Exception {
		// Create languages directory
		Path langDir = tempDir.resolve("languages");
		Files.createDirectory(langDir);

		// Create sample translation file with actual JSON content
		Path enFile = langDir.resolve("en-US.json");
		Map<String, Object> enTranslations = Map.of(
				"vocabulary", Map.of(
						"cancel", "Cancel",
						"ok", "OK"
				)
		);
		Config.save(enFile, enTranslations);

		provider = new CoreTranslationProvider(tempDir);
	}

	@Test
	void loadAll_shouldLoadCoreTranslations() {
		// Act
		Map<String, Map<DiscordLocale, Map<String, String>>> result = provider.load();

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