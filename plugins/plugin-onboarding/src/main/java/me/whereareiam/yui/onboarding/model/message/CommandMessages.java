package me.whereareiam.yui.onboarding.model.message;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * {@code messages/<locale>/commands.yml}: the texts of {@code /verify}.
 */
@Getter
@Setter
public class CommandMessages {
	private Verify verify;

	@Getter
	@Setter
	public static class Verify {
		private String description;
		private String example;
		private Variables variables;
		private Reply started;
		private Reply verified;
		private Reply running;
		private Reply refused;
	}

	@Getter
	@Setter
	public static class Variables {
		private String user;
	}

	/**
	 * A reply to the moderator; {@code {user}} is the member.
	 */
	@Getter
	@Setter
	public static class Reply {
		private String title;
		private List<String> description;
	}
}
