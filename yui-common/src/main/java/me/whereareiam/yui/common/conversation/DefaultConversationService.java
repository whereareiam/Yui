package me.whereareiam.yui.common.conversation;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.conversation.Conversation;
import me.whereareiam.yui.api.conversation.ConversationOptions;
import me.whereareiam.yui.api.conversation.ConversationService;
import me.whereareiam.yui.api.event.conversation.ConversationOpenedEvent;
import me.whereareiam.yui.api.exception.ConversationRefusedException.Reason;
import me.whereareiam.yui.api.exception.ConversationRefusedException;
import me.whereareiam.yui.api.model.config.conversation.Conversations;
import me.whereareiam.yui.api.type.ConversationType;
import me.whereareiam.yui.common.conversation.open.ConversationOpener;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Opens a conversation by trying its setup's types in order, once the limits of its purpose allow one; a member who
 * already has one of that purpose open gets it back.
 */
@Slf4j
@Service
public class DefaultConversationService implements ConversationService {
	private final ObjectProvider<Conversations> config;
	private final ConversationLedger ledger;
	private final ConversationClosing closing;
	private final ApplicationEventPublisher events;
	private final Map<ConversationType, ConversationOpener> openers = new EnumMap<>(ConversationType.class);

	public DefaultConversationService(
			ObjectProvider<Conversations> config,
			ConversationLedger ledger,
			ConversationClosing closing,
			ApplicationEventPublisher events,
			List<ConversationOpener> openers
	) {
		this.config = config;
		this.ledger = ledger;
		this.closing = closing;
		this.events = events;
		openers.forEach(opener -> this.openers.put(opener.type(), opener));
	}

	@Override
	public @NotNull CompletableFuture<Conversation> open(
			@NotNull String purpose,
			@NotNull Collection<Long> members,
			@NotNull ConversationOptions options
	) {
		if (members.isEmpty()) return CompletableFuture.failedFuture(new IllegalArgumentException("A conversation needs a member"));

		Optional<OpenConversation> existing = ledger.find(members.iterator().next(), purpose);
		if (existing.isPresent()) return CompletableFuture.completedFuture(existing.get());

		Set<Long> people = new LinkedHashSet<>(members);
		ConversationSetup setup = ConversationSetup.of(config.getObject(), options, people.size());
		Optional<Reason> refusal = ledger.refusal(people, purpose, setup.limit());
		if (refusal.isPresent())
			return CompletableFuture.failedFuture(new ConversationRefusedException(refusal.get(), "No " + purpose + " conversation now: " + refusal.get()));

		return attempt(purpose, people, setup, 0, null).thenApply(conversation -> {
			ledger.add(conversation);
			closing.watch(conversation);
			events.publishEvent(new ConversationOpenedEvent(conversation));

			return conversation;
		});
	}

	private CompletableFuture<OpenConversation> attempt(String purpose, Set<Long> members, ConversationSetup setup, int index, Throwable last) {
		if (index >= setup.via().size())
			return CompletableFuture.failedFuture(new ConversationRefusedException(Reason.NO_TYPE,
					"No type of " + setup.via() + " could open a " + purpose + " conversation", last));

		ConversationType type = setup.via().get(index);
		return openers.get(type).open(purpose, members, setup)
				.handle((conversation, error) -> {
					if (error == null) return CompletableFuture.completedFuture(conversation);

					log.debug("[Conversations] {} did not open for {}, trying the next type", type, purpose, error);
					return attempt(purpose, members, setup, index + 1, error);
				})
				.thenCompose(next -> next);
	}

	@Override
	public @NotNull Optional<Conversation> find(long member, @NotNull String purpose) {
		return ledger.find(member, purpose).map(conversation -> conversation);
	}

	@Override
	public @NotNull CompletableFuture<Void> close(@NotNull Conversation conversation) {
		if (!(conversation instanceof OpenConversation open)) return CompletableFuture.completedFuture(null);

		return closing.close(open);
	}
}
