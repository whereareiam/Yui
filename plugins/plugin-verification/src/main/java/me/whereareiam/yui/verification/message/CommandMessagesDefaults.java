package me.whereareiam.yui.verification.message;

import me.whereareiam.yui.api.message.MessageDefaults;
import me.whereareiam.yui.verification.model.message.CommandMessages;
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
		CommandMessages.Variables variables = new CommandMessages.Variables();
		variables.setUser("The member to verify");

		CommandMessages.Verify verify = new CommandMessages.Verify();
		verify.setDescription("Starts a verification for a member by hand");
		verify.setExample("/yui verify @Asuna");
		verify.setVariables(variables);
		verify.setStarted(reply("Verification started", "{user} got a conversation to verify in."));
		verify.setVerified(reply("Verified already", "{user} is verified and holds the role again."));
		verify.setRunning(reply("Verifying already", "{user} is in the middle of a verification."));
		verify.setRefused(reply("No verification", "No verification could start for {user}: a bot, not in the server, or no way to talk to them."));

		CommandMessages messages = new CommandMessages();
		messages.setVerify(verify);

		return messages;
	}

	private static CommandMessages.Reply reply(String title, String description) {
		CommandMessages.Reply reply = new CommandMessages.Reply();
		reply.setTitle(title);
		reply.setDescription(List.of(description));

		return reply;
	}
}
