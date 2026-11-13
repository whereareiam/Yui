package me.whereareiam.yui.common.conversation;

import me.whereareiam.yui.common.config.template.ConversationsTemplate;
import me.whereareiam.yui.conversation.ConversationOptions;
import me.whereareiam.yui.model.config.conversation.Conversations;
import me.whereareiam.yui.model.type.Duration;
import me.whereareiam.yui.type.ConversationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ConversationSetupTest {
	private Conversations config;

	@BeforeEach
	void setUp() {
		config = new ConversationsTemplate().supply(new Conversations());
		config.getTypes().getThread().setParent("789");
	}

	@Test
	void of_WithoutOptions_TakesTheDefaults() {
		ConversationSetup setup = ConversationSetup.of(config, ConversationOptions.none(), 1);

		assertEquals(List.of(ConversationType.DIRECT, ConversationType.THREAD, ConversationType.CHANNEL), setup.via());
		assertEquals("789", setup.parent());
		assertSame(config.getDefaults().getClose(), setup.close());
	}

	@Test
	void of_SeveralMembers_NeverTriesADirectMessage() {
		assertEquals(List.of(ConversationType.THREAD, ConversationType.CHANNEL), ConversationSetup.of(config, ConversationOptions.none(), 2).via());
	}

	@Test
	void of_LeavesOutTypesTheServerDoesNotAllow() {
		config.getTypes().getChannel().setEnabled(false);

		ConversationOptions options = ConversationOptions.builder().via(List.of(ConversationType.CHANNEL, ConversationType.THREAD)).build();

		assertEquals(List.of(ConversationType.THREAD), ConversationSetup.of(config, options, 1).via());
	}

	@Test
	void of_OptionsReplaceWhatTheyName() {
		Conversations.Close close = new Conversations.Close();
		close.setIdle(Duration.parse("5m"));

		ConversationSetup setup = ConversationSetup.of(config, ConversationOptions.builder().parent("456").close(close).mention(false).build(), 1);

		assertEquals("456", setup.parent());
		assertSame(close, setup.close());
		assertEquals(false, setup.mention());
		assertSame(config.getDefaults().getLimit(), setup.limit());
	}
}
