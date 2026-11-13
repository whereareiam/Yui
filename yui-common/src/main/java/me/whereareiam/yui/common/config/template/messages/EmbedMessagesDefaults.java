package me.whereareiam.yui.common.config.template.messages;

import me.whereareiam.yui.message.MessageDefaults;
import me.whereareiam.yui.model.config.messages.EmbedMessages;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class EmbedMessagesDefaults implements MessageDefaults<EmbedMessages> {
	@Override
	public @NotNull String file() {
		return "embeds";
	}

	@Override
	public @NotNull EmbedMessages defaults() {
		EmbedMessages.Kind primary = new EmbedMessages.Kind();
		primary.setFooter("Yui");

		EmbedMessages messages = new EmbedMessages();
		messages.setPrimary(primary);

		return messages;
	}
}
