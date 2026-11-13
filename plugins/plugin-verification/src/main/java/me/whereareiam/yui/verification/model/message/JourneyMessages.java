package me.whereareiam.yui.verification.model.message;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * {@code messages/<locale>/journey.yml}: what the member reads while verifying. The rules channel is part of the
 * texts ({@code <#123…>}), so each language can point to its own.
 */
@Getter
@Setter
public class JourneyMessages {
	private Conversation conversation;
	private Step welcome;
	private Step languages;
	private Step end;

	/**
	 * The conversation's thread or channel name ({@code {name}}) and its first message ({@code {mention}}).
	 */
	@Getter
	@Setter
	public static class Conversation {
		private String name;
		private String message;
	}

	@Getter
	@Setter
	public static class Step {
		private String title;
		private List<String> description;
	}
}
