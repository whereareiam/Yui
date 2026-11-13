package me.whereareiam.yui.common.config;

import me.whereareiam.configura.Configura;
import me.whereareiam.yui.common.config.template.SettingsTemplate;
import me.whereareiam.yui.model.config.Commands;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.config.style.Palette;
import me.whereareiam.yui.model.requirement.RequirementEntry;
import me.whereareiam.yui.model.requirement.type.RoleRequirement;
import me.whereareiam.yui.model.requirement.type.ScopeRequirement;
import me.whereareiam.yui.type.ConfigurationType;
import me.whereareiam.yui.type.update.UpdateCheck;
import me.whereareiam.yui.type.update.UpdateWarning;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Reads files the way Yui's providers do, through the instance {@link ConfiguraConfiguration} builds.
 */
class ConfiguraConfigurationTest {
	@TempDir
	Path folder;

	private Configura configura;

	@BeforeEach
	void setUp() {
		configura = ConfiguraConfiguration.build(ConfigurationType.YAML);
	}

	@Test
	void settings_KeepWhatTheFileSaysAndTakeDefaultsForTheRest() throws IOException {
		write("settings.yml", """
				_version: 1
				guild: "856839365938118698"
				locale: de
				updater:
				  interval: 1h30m
				  warn: [updates, localBuilds]
				discord:
				  channels:
				    audit:
				      user_join: "123"
				""");

		Settings settings = configura.merge(file("settings"), new SettingsTemplate().supply(new Settings()));

		assertEquals("856839365938118698", settings.getGuild());
		assertEquals(DiscordLocale.GERMAN, settings.getLocale());
		assertEquals(5400, settings.getUpdater().getInterval().getSeconds());
		assertEquals(Set.of(UpdateWarning.UPDATES, UpdateWarning.LOCAL_BUILDS), settings.getUpdater().getWarn());
		assertEquals(Set.of(UpdateCheck.YUI, UpdateCheck.PLUGINS), settings.getUpdater().getCheck());
		assertEquals(List.of("123"), settings.getDiscord().getChannels().getAudit().get("user_join"));
		assertEquals("SET_YOUR_TOKEN", settings.getToken());
	}

	@Test
	void requirementEntries_AreToldApartByTheirFields() throws IOException {
		write("commands.yml", """
				commands:
				  clear:
				    enabled: true
				    requirements:
				      operator: AND
				      groups:
				        moderators:
				          roles: ["1", "2"]
				        place:
				          scopes: [GUILD]
				""");

		Commands commands = configura.merge(file("commands"), new Commands());
		Map<String, RequirementEntry> groups = commands.getCommands().get("clear").getRequirements().getGroups();

		assertEquals(List.of("1", "2"), assertInstanceOf(RoleRequirement.class, groups.get("moderators")).getRoles());
		assertInstanceOf(ScopeRequirement.class, groups.get("place"));
	}

	@Test
	void colours_AreWrittenAsHex() throws IOException {
		write("palette.yml", "primary: \"#B57E3F\"\n");

		Palette palette = configura.merge(file("palette"), new Palette());
		configura.write(file("palette"), palette);

		assertEquals(new Color(0xB57E3F), palette.getPrimary());
		assertEquals("primary: \"#B57E3F\"", Files.readString(folder.resolve("palette.yml")).lines().filter(line -> line.startsWith("primary")).findFirst().orElseThrow());
	}

	private void write(String name, String content) throws IOException {
		Files.writeString(folder.resolve(name), content);
	}

	private String file(String name) {
		return folder.resolve(name).toString();
	}
}
