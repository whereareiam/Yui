package me.whereareiam.yui.exception;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * A conversation could not be opened: every allowed type failed, or a limit of its purpose keeps it closed.
 */
@Getter
public class ConversationRefusedException extends RuntimeException {
	private final @NotNull Reason reason;

	public ConversationRefusedException(@NotNull Reason reason, @NotNull String message) {
		super(message);
		this.reason = reason;
	}

	public ConversationRefusedException(@NotNull Reason reason, @NotNull String message, Throwable cause) {
		super(message, cause);
		this.reason = reason;
	}

	/**
	 * Why it was refused.
	 */
	public enum Reason {
		/**
		 * No allowed type could be opened.
		 */
		NO_TYPE,

		/**
		 * The member has as many conversations of this purpose open as its limit allows.
		 */
		LIMIT,

		/**
		 * The member's last conversation of this purpose closed too recently.
		 */
		COOLDOWN
	}
}
