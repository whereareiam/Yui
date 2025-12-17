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

class YuiPluginTranslationProviderTest {
	private PluginTranslationProvider provider;

	@TempDir
	Path tempDir;

	@BeforeEach
	void setUp() throws Exception {
		// Create plugin directory structure
		Path musicPlugin = tempDir.resolve("music");
		Files.createDirectory(musicPlugin);

		Path langDir = musicPlugin.resolve("languages");
		Files.createDirectory(langDir);

		// Create sample translation file with actual JSON content
		Path enFile = langDir.resolve("en-US.json");
		Map<String, Object> pluginTranslations = Map.of(
				"vocabulary", Map.of(
						"play", "Play",
						"stop", "Stop"
				)
		);
		Config.save(enFile, pluginTranslations);

		provider = new PluginTranslationProvider(tempDir);
	}

	@Test
	void loadForPlugin_shouldLoadPluginTranslations() {
		// Act
		Map<String, Map<DiscordLocale, Map<String, String>>> result = provider.load("music");

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		assertTrue(result.containsKey("plugin.music."));

		Map<DiscordLocale, Map<String, String>> localeMap = result.get("plugin.music.");
		assertTrue(localeMap.containsKey(DiscordLocale.ENGLISH_US));

		Map<String, String> translations = localeMap.get(DiscordLocale.ENGLISH_US);
		assertEquals("Play", translations.get("vocabulary.play"));
		assertEquals("Stop", translations.get("vocabulary.stop"));
	}
}