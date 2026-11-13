package me.whereareiam.yui.interests.message;

import me.whereareiam.yui.api.message.MessageDefaults;
import me.whereareiam.yui.interests.model.message.TopicMessages;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * Topics have no texts of their own until an installation writes them, since their ids are its own.
 */
@Component
public class TopicMessagesDefaults implements MessageDefaults<TopicMessages> {
	@Override
	public @NotNull String file() {
		return "topics";
	}

	@Override
	public @NotNull TopicMessages defaults() {
		return new TopicMessages();
	}
}
