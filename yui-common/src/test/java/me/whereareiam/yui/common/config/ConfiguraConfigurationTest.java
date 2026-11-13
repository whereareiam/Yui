package me.whereareiam.yui.common.config;

import me.whereareiam.configura.Configura;
import me.whereareiam.configura.exception.ConfigException;
import me.whereareiam.yui.api.model.command.CommandDefinition;
import me.whereareiam.yui.api.model.config.Commands;
import me.whereareiam.yui.api.model.config.audit.AuditTrail;
import me.whereareiam.yui.api.model.config.languages.Languages;
import me.whereareiam.yui.api.model.config.settings.Settings;
import me.whereareiam.yui.api.model.config.style.Palette;
import me.whereareiam.yui.api.model.config.style.StyleColor;
import me.whereareiam.yui.api.model.config.style.embed.EmbedStyle;
import me.whereareiam.yui.api.type.ConfigurationType;
import me.whereareiam.yui.api.type.audit.AuditCategory;
import me.whereareiam.yui.api.type.update.UpdateCheck;
import me.whereareiam.yui.api.type.update.UpdateWarning;
import me.whereareiam.yui.api.util.style.Embeds;
import me.whereareiam.yui.common.config.template.SettingsTemplate;
import me.whereareiam.yui.common.config.template.style.EmbedsTemplate;
import me.whereareiam.yui.common.config.template.style.PaletteTemplate;
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
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
				guild: "856839365938118698"
				locale: de
				updater:
				  interval: 1h30m
				  warn: [updates, localBuilds]
				""");

		Settings settings = configura.merge(file("settings"), new SettingsTemplate().supply(new Settings()));

		assertEquals("856839365938118698", settings.getGuild());
		assertEquals(DiscordLocale.GERMAN, settings.getLocale());
		assertEquals(5400, settings.getUpdater().getInterval().getSeconds());
		assertEquals(Set.of(UpdateWarning.UPDATES, UpdateWarning.LOCAL_BUILDS), settings.getUpdater().getWarn());
		assertEquals(Set.of(UpdateCheck.YUI, UpdateCheck.PLUGINS), settings.getUpdater().getCheck());
		assertEquals("SET_YOUR_TOKEN", settings.getToken());
	}

	@Test
	void commands_ReadTheirRequiresInConditiasForm() throws IOException {
		write("commands.yml", """
				commands:
				  clear:
				    enabled: true
				    requires:
				      - audience: moderator
				      - channel-type: [text, thread]
				    cooldown:
				      enabled: true
				      duration: 10s
				""");

		Commands commands = configura.merge(file("commands"), new Commands());
		CommandDefinition clear = commands.getCommands().get("clear");

		assertEquals(2, clear.getRequires().getConditions().items().size());
		assertEquals(10, clear.getCooldown().getDuration().getSeconds());
	}

	@Test
	void languages_AreKeyedByLocaleInTheFilesOrder() throws IOException {
		write("languages.yml", """
				de:
				  emoji: "🇩🇪"
				en-US:
				  emoji: "🇺🇸"
				  role: "200"
				""");

		Languages languages = configura.merge(file("languages"), new Languages());

		assertEquals(List.of(DiscordLocale.GERMAN, DiscordLocale.ENGLISH_US), List.copyOf(languages.all().keySet()));
		assertEquals("200", languages.get(DiscordLocale.ENGLISH_US).orElseThrow().getRole());
		assertNull(languages.get(DiscordLocale.GERMAN).orElseThrow().getRole());
	}

	@Test
	void languages_ThatAreNoLocale_StopTheFile() throws IOException {
		write("languages.yml", "german:\n  emoji: x\n");

		assertThrows(ConfigException.class, () -> configura.merge(file("languages"), new Languages()));
	}

	@Test
	void audit_PostsCategoriesAndSendsEntriesElsewhereOrNowhere() throws IOException {
		write("audit.yml", """
				categories:
				  members:
				    channel: "100"
				  system:
				    channel: "300"
				entries:
				  member.leave: off
				  onboarding.failed: system
				""");

		AuditTrail trail = configura.merge(file("audit"), new AuditTrail());

		assertEquals(Optional.of("100"), trail.channel(AuditCategory.MEMBERS, "member.join"));
		assertEquals(Optional.empty(), trail.channel(AuditCategory.MEMBERS, "member.leave"));
		assertEquals(Optional.of("300"), trail.channel(AuditCategory.MODERATION, "onboarding.failed"));
		assertEquals(Optional.empty(), trail.channel(AuditCategory.MODERATION, "member.kick"));
	}

	@Test
	void palette_NamesItsColoursAndKeepsYuisOwn() throws IOException {
		write("palette.yml", "violet: \"#8A5CF6\"\nred: \"#E03131\"\n");

		Palette palette = configura.merge(file("palette"), new PaletteTemplate().supply(new Palette()));

		assertEquals(new Color(0x8A5CF6), palette.get("violet").orElseThrow());
		assertEquals(new Color(0xE03131), palette.get("red").orElseThrow());
		assertEquals(new Color(0xB57E3F), palette.get("brand").orElseThrow());
	}

	@Test
	void embeds_TakeAPaletteNameOrAHex() throws IOException {
		write("embeds.yml", """
				error:
				  color: "#E03131"
				  thumbnail: "https://example.org/x.png"
				info:
				  color: violet
				""");

		EmbedStyle style = configura.merge(file("embeds"), new EmbedsTemplate().supply(new EmbedStyle()));

		assertEquals(new StyleColor.Hex(0xE03131), style.getError().getColor());
		assertEquals("https://example.org/x.png", style.getError().getThumbnail());
		assertEquals(StyleColor.named("violet"), style.getInfo().getColor());
		assertEquals(StyleColor.named("brand"), style.getPrimary().getColor());
	}

	@Test
	void embeds_WhoseColourIsNotInThePalette_AreRefused() {
		EmbedStyle style = new EmbedsTemplate().supply(new EmbedStyle());
		style.getInfo().setColor(StyleColor.named("violet"));
		Palette palette = new PaletteTemplate().supply(new Palette());

		IllegalStateException refused = assertThrows(IllegalStateException.class, () -> new Embeds(palette, style));
		assertEquals("style/embeds.yml: info.color names no colour 'violet' in style/palette.yml", refused.getMessage());
	}

	private void write(String name, String content) throws IOException {
		Files.writeString(folder.resolve(name), content);
	}

	private String file(String name) {
		return folder.resolve(name).toString();
	}
}
