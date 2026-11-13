package me.whereareiam.yui.model.config.messages;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * {@code messages/<locale>/conversations.yml}: the texts of conversations Yui opens.
 */
@Getter
@Setter
public class ConversationMessages {
	private Close close;

	/**
	 * The line a transcript is posted with; {@code {purpose}} is what the conversation was for.
	 */
	private String transcript;

	/**
	 * The warning before a conversation closes on its own; {@code {seconds}} is how long it has left.
	 */
	@Getter
	@Setter
	public static class Close {
		private String title;
		private List<String> description;
	}
}
