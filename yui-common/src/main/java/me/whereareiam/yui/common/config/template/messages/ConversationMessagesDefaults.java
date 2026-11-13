package me.whereareiam.yui.common.config.template.messages;

import me.whereareiam.yui.api.message.MessageDefaults;
import me.whereareiam.yui.api.model.config.messages.ConversationMessages;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConversationMessagesDefaults implements MessageDefaults<ConversationMessages> {
	@Override
	public @NotNull String file() {
		return "conversations";
	}

	@Override
	public @NotNull ConversationMessages defaults() {
		ConversationMessages.Close close = new ConversationMessages.Close();
		close.setTitle("Closing conversation");
		close.setDescription(List.of("This conversation will be closed in *{seconds}* seconds."));

		ConversationMessages messages = new ConversationMessages();
		messages.setClose(close);
		messages.setTranscript("Transcript of the {purpose} conversation");

		return messages;
	}
}
