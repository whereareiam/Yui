package me.whereareiam.yui.verification.journey;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.whereareiam.yui.api.conversation.Conversation;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.Nullable;

/**
 * One member's verification while it runs: the conversation it happens in and the message its steps edit.
 */
@Getter
@RequiredArgsConstructor
public class VerificationState {
	private final long member;
	private final Conversation conversation;

	/**
	 * Who started it by hand, or null when Yui did.
	 */
	private final @Nullable Long initiator;

	/**
	 * The message the steps show and edit, once it is sent.
	 */
	@Setter
	private volatile @Nullable Message message;
}
