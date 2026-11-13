package me.whereareiam.yui.example.config;

import me.whereareiam.yui.api.message.MessageDefaults;
import me.whereareiam.yui.example.model.message.CommandMessages;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * The English texts of {@code commands.yml}; an installation translates them in {@code messages/<locale>/}.
 */
@Component
public class CommandMessagesDefaults implements MessageDefaults<CommandMessages> {
	@Override
	public @NotNull String file() {
		return "commands";
	}

	@Override
	public @NotNull CommandMessages defaults() {
		CommandMessages.Hello hello = new CommandMessages.Hello();
		hello.setDescription("Says hello");
		hello.setExample("/hello");
		hello.setReply("Hello {mention} {emoji}");

		CommandMessages messages = new CommandMessages();
		messages.setHello(hello);

		return messages;
	}
}
