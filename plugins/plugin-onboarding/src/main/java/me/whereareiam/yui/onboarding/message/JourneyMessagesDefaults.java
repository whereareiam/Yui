package me.whereareiam.yui.onboarding.message;

import me.whereareiam.yui.api.message.MessageDefaults;
import me.whereareiam.yui.onboarding.model.message.JourneyMessages;
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
		JourneyMessages.Conversation conversation = new JourneyMessages.Conversation();
		conversation.setName("onboarding-{name}");
		conversation.setMessage("{mention}, a few steps and you are in.");

		JourneyMessages messages = new JourneyMessages();
		messages.setConversation(conversation);
		messages.setWelcome(step("Welcome to the server",
				"Welcome {mention}! Before you can start chatting, pick the language you speak best.",
				"",
				"Click your language below, then continue."
		));
		messages.setLanguages(step("More languages",
				"Pick every other language you understand, if any.",
				"",
				"Each one opens that language's channels to you. Continue when you are done."
		));
		messages.setEnd(step("You're all set",
				"Thank you {mention}, you are verified and can join the conversation.",
				"",
				"Please read the server's rules before you start. Enjoy your time here!"
		));

		return messages;
	}

	private static JourneyMessages.Step step(String title, String... description) {
		JourneyMessages.Step step = new JourneyMessages.Step();
		step.setTitle(title);
		step.setDescription(List.of(description));

		return step;
	}
}
