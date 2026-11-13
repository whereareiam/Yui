package me.whereareiam.yui.onboarding.message;

import me.whereareiam.yui.api.message.MessageDefaults;
import me.whereareiam.yui.onboarding.model.message.AuditMessages;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuditMessagesDefaults implements MessageDefaults<AuditMessages> {
	@Override
	public @NotNull String file() {
		return "audit";
	}

	@Override
	public @NotNull AuditMessages defaults() {
		AuditMessages messages = new AuditMessages();
		messages.setStarted(entry("Onboarding started",
				"{initiator, select, none {{mention} began onboarding.} other {{initiator} started onboarding for {mention}.}}"));
		messages.setCompleted(entry("Onboarding completed", "{mention} is verified."));
		messages.setFailed(entry("Onboarding failed", "The onboarding of {mention} failed."));
		messages.setAbandoned(entry("Onboarding abandoned", "{mention} left onboarding before finishing it."));
		messages.setTimeout(entry("Onboarding timed out", "{mention} did not finish onboarding in time."));

		return messages;
	}

	private static AuditMessages.Entry entry(String title, String description) {
		AuditMessages.Entry entry = new AuditMessages.Entry();
		entry.setTitle(title);
		entry.setDescription(List.of(description));

		return entry;
	}
}
