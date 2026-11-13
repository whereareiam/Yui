package me.whereareiam.yui.onboarding.message;

import me.whereareiam.yui.api.message.MessageDefaults;
import me.whereareiam.yui.onboarding.model.message.SettingsMessages;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class SettingsMessagesDefaults implements MessageDefaults<SettingsMessages> {
	@Override
	public @NotNull String file() {
		return "settings";
	}

	@Override
	public @NotNull SettingsMessages defaults() {
		SettingsMessages.Deadline deadline = new SettingsMessages.Deadline();
		deadline.setKick("Did not finish onboarding in time");

		SettingsMessages messages = new SettingsMessages();
		messages.setDeadline(deadline);

		return messages;
	}
}
