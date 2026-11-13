package me.whereareiam.yui.interests.model;

import me.whereareiam.yui.api.type.ConfigurationType;
import me.whereareiam.yui.common.config.ConfiguraConfiguration;
import me.whereareiam.yui.common.config.ConfiguraReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reads {@code topics.yml} the way Yui reads it for the plugin.
 */
class TopicsTest {
	@TempDir
	Path folder;

	private final ConfiguraReader reader = new ConfiguraReader(ConfiguraConfiguration.build(ConfigurationType.YAML));

	@Test
	void topics_KeepTheirRolesAndTheFilesOrder() throws IOException {
		Files.writeString(folder.resolve("topics.yml"), """
				minecraft:
				  role: "2"
				development:
				  role: "1"
				""");

		Topics topics = new Topics(reader.readMap(folder.resolve("topics"), Topic.class));

		assertEquals(List.of("minecraft", "development"), List.copyOf(topics.all().keySet()));
		assertEquals("1", topics.all().get("development").getRole());
	}

	@Test
	void withoutAFile_ThereAreNoTopics() {
		assertTrue(reader.readMap(folder.resolve("topics"), Topic.class).isEmpty());
	}
}
