package me.whereareiam.yui.common.conversation;

import me.whereareiam.yui.api.conversation.ConversationOptions;
import me.whereareiam.yui.api.exception.ConversationRefusedException.Reason;
import me.whereareiam.yui.api.model.config.conversation.Conversations;
import me.whereareiam.yui.api.type.ConversationType;
import me.whereareiam.yui.common.config.template.ConversationsTemplate;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ConversationLedgerTest {
	private static final long MEMBER = 1L;

	private final MutableClock clock = new MutableClock();
	private final ConversationLedger ledger = new ConversationLedger(clock);
	private final Conversations.Limit limit = new ConversationsTemplate().supply(new Conversations()).getDefaults().getLimit();

	@Test
	void refusal_WhileOneIsOpen_IsTheLimit() {
		ledger.add(conversation("onboarding"));

		assertEquals(Optional.of(Reason.LIMIT), ledger.refusal(List.of(MEMBER), "onboarding", limit));
		assertEquals(Optional.empty(), ledger.refusal(List.of(MEMBER), "support", limit));
	}

	@Test
	void refusal_SoonAfterOneClosed_IsTheCooldown() {
		OpenConversation conversation = conversation("onboarding");
		ledger.add(conversation);
		ledger.remove(conversation);

		assertEquals(Optional.of(Reason.COOLDOWN), ledger.refusal(List.of(MEMBER), "onboarding", limit));

		clock.now = clock.now.plusSeconds(limit.getCooldown().getSeconds() + 1);
		assertEquals(Optional.empty(), ledger.refusal(List.of(MEMBER), "onboarding", limit));
	}

	@Test
	void find_GivesTheMembersOpenConversationOfThatPurpose() {
		OpenConversation conversation = conversation("onboarding");
		ledger.add(conversation);

		assertEquals(Optional.of(conversation), ledger.find(MEMBER, "onboarding"));
		assertEquals(Optional.empty(), ledger.find(2L, "onboarding"));
	}

	private OpenConversation conversation(String purpose) {
		ConversationSetup setup = ConversationSetup.of(new ConversationsTemplate().supply(new Conversations()), ConversationOptions.none(), 1);

		return new OpenConversation(purpose, Set.of(MEMBER), ConversationType.DIRECT, mock(MessageChannel.class), setup,
				() -> CompletableFuture.completedFuture(null));
	}

	private static final class MutableClock extends Clock {
		private Instant now = Instant.parse("2026-11-13T12:00:00Z");

		@Override
		public ZoneOffset getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return now;
		}
	}
}
