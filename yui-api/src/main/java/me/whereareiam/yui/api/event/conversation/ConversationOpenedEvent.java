package me.whereareiam.yui.api.event.conversation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.conversation.Conversation;
import org.jetbrains.annotations.NotNull;

/**
 * A conversation opened: its thread, channel or direct message is there.
 */
@Getter
@RequiredArgsConstructor
public class ConversationOpenedEvent {
	private final @NotNull Conversation conversation;
}
