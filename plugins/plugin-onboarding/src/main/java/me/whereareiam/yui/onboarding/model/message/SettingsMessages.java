package me.whereareiam.yui.onboarding.model.message;

import lombok.Getter;
import lombok.Setter;

/**
 * {@code messages/<locale>/settings.yml}: texts of what {@code settings.yml} decides, such as the reason a member is
 * kicked for after the deadline.
 */
@Getter
@Setter
public class SettingsMessages {
	private Deadline deadline;

	@Getter
	@Setter
	public static class Deadline {
		private String kick;
	}
}
