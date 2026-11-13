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
	void anOldSettingsFile_NeedsItsMigration() throws IOException {
		writeOldSettings("POSTGRESQL");

		assertEquals(List.of("yui/config/settings 1 agreed-shape", "yui/config/settings 2 audit-into-its-file"), new ConfigUpgrade(configura, folder).pending());
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

		assertEquals(2, settings.path("_version").asInt());
		assertEquals("secret", settings.path("token").asText());
		assertEquals("856839365938118698", settings.path("guild").asText());
		assertEquals("GUILD_MEMBERS", settings.path("intents").path(0).asText());
		assertFalse(settings.path("discord").has("token"));
		assertFalse(settings.path("discord").has("channels"));
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
	void run_GivesEachAuditCategoryTheChannelOfItsFirstOldType() throws IOException {
		Files.writeString(folder.resolve("settings.yml"), """
				discord:
				  channels:
				    tempChannelCategories: ["9"]
				    audit:
				      user_join: ["ru:11", "10"]
				      user_kick: "20"
				      update_*_available: "30"
				""");

		new ConfigUpgrade(configura, folder).run();
		JsonNode audit = configura.readNode(folder.resolve("audit.yml"));
		JsonNode settings = configura.readNode(folder.resolve("settings.yml"));

		assertEquals("10", audit.path("categories").path("members").path("channel").asText());
		assertEquals("20", audit.path("categories").path("moderation").path("channel").asText());
		assertEquals("30", audit.path("categories").path("system").path("channel").asText());
		assertEquals("off", audit.path("entries").path("member.leave").asText());
		assertEquals("[\"9\"]", settings.path("discord").path("channels").path("tempChannelCategories").toString());
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

	@Test
	void run_LeavesAFileInItsNewShapeAsItIs() throws IOException {
		Files.writeString(folder.resolve("settings.yml"), """
				token: secret
				guild: "1"
				updater:
				  check: [plugins]
				  warn: []
				""");

		new ConfigUpgrade(configura, folder).run();
		JsonNode settings = configura.readNode(folder.resolve("settings.yml"));

		assertEquals("[\"plugins\"]", settings.path("updater").path("check").toString());
		assertEquals("[]", settings.path("updater").path("warn").toString());
		assertFalse(Files.exists(folder.resolve("persistence.yml")));
	}

	@Test
	void run_ListsLanguagesAtTheTopAndLeavesOutDisabledOnes() throws IOException {
		writeOldSettings("H2");
		Files.writeString(folder.resolve("languages.yml"), """
				languages:
				  en-US:
				    enabled: true
				    role: 200
				    displayName: English
				    emoji: "🇺🇸"
				  de:
				    enabled: false
				    emoji: "🇩🇪"
				  fr:
				    enabled: true
				settings:
				  cacheEnabled: true
				""");

		new ConfigUpgrade(configura, folder).run();
		JsonNode languages = configura.readNode(folder.resolve("languages.yml"));

		assertEquals(1, languages.path("_version").asInt());
		assertEquals("200", languages.path("en-US").path("role").asText());
		assertEquals("🇺🇸", languages.path("en-US").path("emoji").asText());
		assertFalse(languages.path("en-US").has("displayName"));
		assertFalse(languages.has("de"));
		assertTrue(languages.has("fr"));
		assertFalse(languages.has("languages"));
		assertFalse(languages.has("settings"));
	}

	@Test
	void run_MovesTheStyleAndKeepsEveryKindsColour() throws IOException {
		writeOldSettings("H2");
		Files.createDirectories(folder.resolve("styles"));
		Files.writeString(folder.resolve("styles/palette.yml"), "primary: \"#B57E3F\"\nerror: \"#123456\"\n");
		Files.writeString(folder.resolve("styles/embeds.yml"), """
				primary:
				  author: { name: "", iconUrl: "", url: "" }
				  footer: { text: "Yui", iconUrl: "" }
				  titleUrl: ""
				  thumbnailUrl: "https://example.org/t.png"
				  imageUrl: ""
				  timestamp: true
				secondary:
				  footer: { text: "", iconUrl: "" }
				  timestamp: false
				""");

		new ConfigUpgrade(configura, folder).run();
		JsonNode palette = configura.readNode(folder.resolve("style/palette.yml"));
		JsonNode embeds = configura.readNode(folder.resolve("style/embeds.yml"));

		assertEquals(1, palette.path("_version").asInt());
		assertEquals(1, embeds.path("_version").asInt());
		assertEquals("#123456", palette.path("error").asText());
		assertEquals("primary", embeds.path("primary").path("color").asText());
		assertEquals("Yui", embeds.path("primary").path("footer").path("text").asText());
		assertEquals("https://example.org/t.png", embeds.path("primary").path("thumbnail").asText());
		assertFalse(embeds.path("primary").has("author"));
		assertFalse(embeds.path("primary").has("link"));
		assertFalse(embeds.has("secondary"));
		assertFalse(Files.exists(folder.resolve("styles")));
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
