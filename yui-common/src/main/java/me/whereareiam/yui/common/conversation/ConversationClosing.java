package me.whereareiam.yui.common.conversation;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.event.conversation.ConversationClosedEvent;
import me.whereareiam.yui.api.message.Translatable;
import me.whereareiam.yui.api.model.config.conversation.Conversations;
import me.whereareiam.yui.api.model.type.Duration;
import me.whereareiam.yui.api.util.style.StyleKit;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Closes conversations: at once when code asks, and on their own {@code idle} after the last message or
 * {@code after} they opened, with the close warning {@code warn} before. A closing conversation's transcript is
 * made first, while its messages are still there.
 */
@Slf4j
@Component
public class ConversationClosing {
	private final ConversationLedger ledger;
	private final ConversationTranscript transcript;
	private final ApplicationEventPublisher events;
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
		Thread thread = new Thread(runnable, "conversation-closing");
		thread.setDaemon(true);
		return thread;
	});
	private final Map<OpenConversation, ScheduledFuture<?>> idle = new ConcurrentHashMap<>();
	private final Map<OpenConversation, ScheduledFuture<?>> lifetime = new ConcurrentHashMap<>();

	public ConversationClosing(ConversationLedger ledger, ConversationTranscript transcript, ApplicationEventPublisher events) {
		this.ledger = ledger;
		this.transcript = transcript;
		this.events = events;
	}

	/**
	 * Starts counting down a conversation's idle time and lifetime.
	 */
	void watch(OpenConversation conversation) {
		Duration after = conversation.getSetup().close().getAfter();
		if (after != null) lifetime.put(conversation, schedule(conversation, after));

		touched(conversation);
	}

	/**
	 * A message arrived: the idle time starts again.
	 */
	void touched(OpenConversation conversation) {
		Duration wait = conversation.getSetup().close().getIdle();
		if (wait == null) return;

		ScheduledFuture<?> previous = idle.put(conversation, schedule(conversation, wait));
		if (previous != null) previous.cancel(false);
	}

	/**
	 * Closes a conversation now, without a warning.
	 */
	CompletableFuture<Void> close(OpenConversation conversation) {
		forget(conversation);
		if (ledger.remove(conversation)) events.publishEvent(new ConversationClosedEvent(conversation));

		return transcript.send(conversation).thenCompose(ignored -> conversation.shut());
	}

	/**
	 * Forgets a conversation whose channel is gone, without closing anything.
	 */
	void lost(OpenConversation conversation) {
		forget(conversation);
		if (ledger.remove(conversation)) events.publishEvent(new ConversationClosedEvent(conversation));
	}

	private ScheduledFuture<?> schedule(OpenConversation conversation, Duration delay) {
		return scheduler.schedule(() -> closeWarned(conversation), delay.getSeconds(), TimeUnit.SECONDS);
	}

	private void closeWarned(OpenConversation conversation) {
		forget(conversation);
		Conversations.Close close = conversation.getSetup().close();
		if (close.getWarn() == null || close.getWarn().getSeconds() <= 0) {
			close(conversation);
			return;
		}

		warn(conversation, close.getWarn()).whenComplete((sent, error) ->
				scheduler.schedule(() -> close(conversation), close.getWarn().getSeconds(), TimeUnit.SECONDS));
	}

	private CompletableFuture<?> warn(OpenConversation conversation, Duration warn) {
		long member = conversation.getMembers().iterator().next();
		Translatable title = Translatable.text("conversations.close.title");
		Translatable description = Translatable.text("conversations.close.description").with("seconds", warn.getSeconds());

		return conversation.getChannel().sendMessageEmbeds(StyleKit.embeds().warning()
						.setTitle(title.resolve(member))
						.setDescription(description.resolve(member))
						.build())
				.submit()
				.exceptionally(error -> {
					log.debug("[Conversations] Could not warn before closing {}", conversation.getChannel(), error);
					return null;
				});
	}

	private void forget(OpenConversation conversation) {
		ScheduledFuture<?> waiting = idle.remove(conversation);
		if (waiting != null) waiting.cancel(false);

		ScheduledFuture<?> ending = lifetime.remove(conversation);
		if (ending != null) ending.cancel(false);
	}

	@PreDestroy
	void stop() {
		scheduler.shutdownNow();
	}
}
