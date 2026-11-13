package me.whereareiam.yui.api.event.conversation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.conversation.Conversation;
import org.jetbrains.annotations.NotNull;

/**
 * A conversation closed: code closed it, it was idle or open for too long, or its channel was deleted. Published once
 * per conversation, so whatever ran in it can stop.
 *
 * <pre>{@code
 * @EventListener
 * public void onClosed(ConversationClosedEvent event) {
 *     if (event.getConversation().getPurpose().equals(PURPOSE)) journeys.cancel(sessionOf(event.getConversation()));
 * }
 * }</pre>
 */
@Getter
@RequiredArgsConstructor
public class ConversationClosedEvent {
	private final @NotNull Conversation conversation;
}
