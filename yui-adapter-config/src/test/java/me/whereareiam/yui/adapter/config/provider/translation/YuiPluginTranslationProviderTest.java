package me.whereareiam.yui.adapter.config.provider.translation;

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
class YuiPluginTranslationProviderTest {
	@Mock
	private ObjectMapper objectMapper;

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

		Path enFile = langDir.resolve("en-US.json");
		Files.writeString(enFile, "{}");

		// Setup mock behavior
		Map<String, Object> pluginTranslations = Map.of(
				"vocabulary", Map.of(
						"play", "Play",
						"stop", "Stop"
				)
		);
		when(objectMapper.readValue(any(File.class), any(TypeReference.class)))
				.thenReturn(pluginTranslations);

		provider = new PluginTranslationProvider(tempDir, objectMapper);
	}

	@Test
	void loadAll_shouldLoadPluginTranslations() {
		// Act
		Map<String, Map<DiscordLocale, Map<String, String>>> result = provider.loadAll();

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