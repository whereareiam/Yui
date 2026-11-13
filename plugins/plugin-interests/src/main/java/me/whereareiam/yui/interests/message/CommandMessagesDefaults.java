package me.whereareiam.yui.interests.message;

import me.whereareiam.yui.api.message.MessageDefaults;
import me.whereareiam.yui.interests.model.message.CommandMessages;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommandMessagesDefaults implements MessageDefaults<CommandMessages> {
	@Override
	public @NotNull String file() {
		return "commands";
	}

	@Override
	public @NotNull CommandMessages defaults() {
		CommandMessages.Topics topics = new CommandMessages.Topics();
		topics.setDescription("Change the topics you are here for");
		topics.setExample("/topics");
		topics.setMenu(reply("Your topics", "Pick the topics you are here for; each one opens its channels to you."));
		topics.setSaved(reply("Topics saved", "Your channels follow your topics now."));

		CommandMessages messages = new CommandMessages();
		messages.setTopics(topics);

		return messages;
	}

	private static CommandMessages.Reply reply(String title, String description) {
		CommandMessages.Reply reply = new CommandMessages.Reply();
		reply.setTitle(title);
		reply.setDescription(List.of(description));

		return reply;
	}
}
