package me.whereareiam.yui.verification.message;

import me.whereareiam.yui.api.message.MessageDefaults;
import me.whereareiam.yui.verification.model.message.AuditMessages;
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
		messages.setStarted(entry("Verification started",
				"{initiator, select, none {{mention} began verifying.} other {{initiator} started a verification for {mention}.}}"));
		messages.setCompleted(entry("Verification completed", "{mention} is verified."));
		messages.setFailed(entry("Verification failed", "The verification of {mention} failed."));
		messages.setAbandoned(entry("Verification abandoned", "{mention} left the verification before finishing it."));
		messages.setTimeout(entry("Verification timed out", "{mention} did not finish verifying in time."));

		return messages;
	}

	private static AuditMessages.Entry entry(String title, String description) {
		AuditMessages.Entry entry = new AuditMessages.Entry();
		entry.setTitle(title);
		entry.setDescription(List.of(description));

		return entry;
	}
}
