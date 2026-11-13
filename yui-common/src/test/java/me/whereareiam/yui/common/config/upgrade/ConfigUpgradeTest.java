package me.whereareiam.yui.common.config.upgrade;

import com.fasterxml.jackson.databind.JsonNode;
import me.whereareiam.configura.Configura;
import me.whereareiam.yui.common.config.ConfiguraConfiguration;
import me.whereareiam.yui.type.ConfigurationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigUpgradeTest {
	@TempDir
	Path folder;

	private Configura configura;

	@BeforeEach
	void setUp() {
		configura = ConfiguraConfiguration.build(ConfigurationType.YAML);
	}

	@Test
	void anOldFolder_NeedsMigrationOne() throws IOException {
		writeOldSettings("POSTGRESQL");

		assertEquals(List.of("1 settings-and-persistence"), new ConfigUpgrade(configura, folder).pending());
	}

	@Test
	void aNewFolder_NeedsNothing() {
		assertTrue(new ConfigUpgrade(configura, folder).pending().isEmpty());
	}

	@Test
	void run_MovesDiscordValuesUpAndTheDatabaseIntoPersistence() throws IOException {
		writeOldSettings("POSTGRESQL");

		new ConfigUpgrade(configura, folder).run();
		JsonNode settings = configura.readNode(folder.resolve("settings.yml"));
		JsonNode persistence = configura.readNode(folder.resolve("persistence.yml"));

		assertEquals(1, settings.path("_version").asInt());
		assertEquals("secret", settings.path("token").asText());
		assertEquals("856839365938118698", settings.path("guild").asText());
		assertEquals("GUILD_MEMBERS", settings.path("intents").path(0).asText());
		assertFalse(settings.path("discord").has("token"));
		assertEquals("123", settings.path("discord").path("channels").path("audit").path("user_join").asText());
		assertEquals("[\"yui\"]", settings.path("updater").path("check").toString());
		assertEquals("[\"updates\",\"devBuilds\"]", settings.path("updater").path("warn").toString());
		assertFalse(settings.has("database"));

		assertEquals("POSTGRESQL", persistence.path("type").asText());
		assertEquals("db.local", persistence.path("host").asText());
		assertEquals(5433, persistence.path("port").asInt());
		assertFalse(persistence.has("file"));
		assertEquals(2, persistence.path("pool").path("size").path("min").asInt());
		assertEquals("30s", persistence.path("pool").path("timeouts").path("connect").asText());
		assertEquals("30m", persistence.path("pool").path("timeouts").path("lifetime").asText());

		assertTrue(new ConfigUpgrade(configura, folder).pending().isEmpty());
	}

	@Test
	void run_KeepsOnlyTheFileOfAnH2Database() throws IOException {
		writeOldSettings("H2");

		new ConfigUpgrade(configura, folder).run();
		JsonNode persistence = configura.readNode(folder.resolve("persistence.yml"));

		assertEquals("H2", persistence.path("type").asText());
		assertEquals("database/yui", persistence.path("file").asText());
		assertFalse(persistence.has("host"));
	}

	private void writeOldSettings(String databaseType) throws IOException {
		Files.writeString(folder.resolve("settings.yml"), """
				locale: en-US
				discord:
				  guildId: "856839365938118698"
				  token: secret
				  intents: [GUILD_MEMBERS]
				  channels:
				    audit:
				      user_join: "123"
				database:
				  type: %s
				  file: database/yui
				  hostname: db.local
				  port: 5433
				  database: yui
				  username: yui
				  password: pw
				  hikari:
				    minimumIdle: 2
				    maximumPoolSize: 10
				    connectionTimeout: 30000
				    idleTimeout: 600000
				    maxLifetime: 1800000
				updater:
				  checkForUpdates: true
				  checkPluginUpdates: false
				  warnAboutUpdates: true
				  warnAboutLocalBuilds: false
				  warnAboutDevBuilds: true
				  interval: 1d
				""".formatted(databaseType));
	}
}
