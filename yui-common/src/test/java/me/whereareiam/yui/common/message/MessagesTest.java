package me.whereareiam.yui.common.message;

import me.whereareiam.semantica.Text;
import me.whereareiam.semantica.catalog.Layer;
import me.whereareiam.semantica.common.Semantica;
import me.whereareiam.semantica.issue.Issue;
import me.whereareiam.yui.common.config.template.messages.AuditMessagesDefaults;
import me.whereareiam.yui.common.config.template.messages.CommandMessagesDefaults;
import me.whereareiam.yui.common.config.template.messages.ConversationMessagesDefaults;
import me.whereareiam.yui.common.config.template.messages.ErrorMessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.WordsDefaults;
import me.whereareiam.yui.common.config.template.messages.command.ClearCommandMessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.command.HelpCommandMessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.command.LanguageCommandMessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.command.MainCommandMessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.command.PluginCommandMessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.command.ReloadCommandMessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.command.StatusCommandMessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.command.UpdateCheckCommandMessagesTemplate;
import me.whereareiam.yui.message.MessageDefaults;
import me.whereareiam.yui.model.config.messages.Words;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Yui's texts as Semantica serves them: the defaults, files over them, plugins' layers and Discord's rendering.
 */
class MessagesTest {
	@TempDir
	Path folder;

	private Semantica semantica;
	private SemanticaTextService texts;

	@BeforeEach
	void setUp() throws IOException {
		Files.createDirectories(folder.resolve("messages/de"));
		Files.writeString(folder.resolve("messages/de/words.yml"), "cancel: \"Abbrechen\"\n");

		semantica = new MessageConfiguration().semantica(defaults(), folder.resolve("messages"));
		texts = new SemanticaTextService(semantica);
	}

	@AfterEach
	void tearDown() {
		semantica.close();
	}

	@Test
	void everyDefaultText_IsAValidMessage() {
		List<Issue> issues = semantica.getCatalog().getIssues();

		assertTrue(issues.isEmpty(), () -> "Issues: " + issues);
	}

	@Test
	void aFile_OverridesTheDefaultInItsLocale() {
		assertEquals("Abbrechen", texts.resolve(Text.of("words.cancel"), DiscordLocale.GERMAN));
		assertEquals("Cancel", texts.resolve(Text.of("words.cancel"), DiscordLocale.FRENCH));
	}

	@Test
	void values_AreInsertedWhileTheTextKeepsItsMarkdown() {
		String closing = texts.resolve(Text.of("conversations.close.description").with("seconds", 30), DiscordLocale.ENGLISH_US);

		assertEquals("This conversation will be closed in *30* seconds.", closing);
	}

	@Test
	void values_AreEscapedForDiscord() {
		semantica.add(Layer.owned("test", new DefaultsSource("test", "test", List.of(new Echo()), MessageConfiguration.TEXTS.mapper())));

		assertEquals("\\*\\*Bob\\*\\*", texts.resolve(Text.of("test.echo.cancel").with("value", "**Bob**"), DiscordLocale.ENGLISH_US));
	}

	@Test
	void aPlugin_GetsItsOwnKeys() throws IOException {
		Path plugin = folder.resolve("plugins/verification/messages");
		Files.createDirectories(plugin.resolve("de"));
		Files.writeString(plugin.resolve("de/words.yml"), "cancel: \"Verwerfen\"\n");

		new SemanticaMessageLayers(semantica).add("verification", List.of(new WordsDefaults()), plugin);

		assertEquals("Cancel", texts.resolve(Text.of("verification.words.cancel"), DiscordLocale.ENGLISH_US));
		assertEquals("Verwerfen", texts.resolve(Text.of("verification.words.cancel"), DiscordLocale.GERMAN));
		assertEquals("Abbrechen", texts.resolve(Text.of("words.cancel"), DiscordLocale.GERMAN));
	}

	private static List<MessageDefaults<?>> defaults() {
		return List.of(
				new CommandMessagesDefaults(
						new ErrorMessagesTemplate(),
						new MainCommandMessagesTemplate(),
						new HelpCommandMessagesTemplate(),
						new ClearCommandMessagesTemplate(),
						new ReloadCommandMessagesTemplate(),
						new PluginCommandMessagesTemplate(),
						new StatusCommandMessagesTemplate(),
						new LanguageCommandMessagesTemplate(),
						new UpdateCheckCommandMessagesTemplate()
				),
				new AuditMessagesDefaults(),
				new ConversationMessagesDefaults(),
				new WordsDefaults()
		);
	}

	private static final class Echo implements MessageDefaults<Words> {
		@Override
		public @NotNull String file() {
			return "echo";
		}

		@Override
		public @NotNull Words defaults() {
			Words words = new Words();
			words.setCancel("{value}");

			return words;
		}
	}
}
