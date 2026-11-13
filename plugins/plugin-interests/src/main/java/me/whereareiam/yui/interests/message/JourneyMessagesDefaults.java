package me.whereareiam.yui.interests.message;

import me.whereareiam.yui.api.message.MessageDefaults;
import me.whereareiam.yui.interests.model.message.JourneyMessages;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JourneyMessagesDefaults implements MessageDefaults<JourneyMessages> {
	@Override
	public @NotNull String file() {
		return "journey";
	}

	@Override
	public @NotNull JourneyMessages defaults() {
		JourneyMessages.Topics topics = new JourneyMessages.Topics();
		topics.setTitle("What brings you here?");
		topics.setDescription(List.of(
				"Pick the topics you are here for, as many as you like or none.",
				"",
				"Each one opens its channels to you. You can change them later with `/topics`."
		));
		topics.setPlaceholder("Pick topics…");

		JourneyMessages messages = new JourneyMessages();
		messages.setTopics(topics);

		return messages;
	}
}
