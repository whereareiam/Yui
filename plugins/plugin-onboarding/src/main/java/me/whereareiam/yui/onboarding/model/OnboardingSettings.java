package me.whereareiam.yui.onboarding.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.api.model.type.Duration;
import me.whereareiam.yui.onboarding.type.DeadlineAction;
import org.jetbrains.annotations.Nullable;

/**
 * {@code plugins/onboarding/settings.yml}: when onboarding starts, how long a member has to finish it and what
 * happens after, and the role a verified member holds.
 *
 * <pre>{@code
 * triggers:
 *   member-join: true
 *   startup: true
 *   member-cleared: true
 * deadline:
 *   enabled: true
 *   after: 1d
 *   action: kick
 * verified-role: "123456789012345678"
 * }</pre>
 */
@Getter
@Setter
public class OnboardingSettings {
	private Triggers triggers = new Triggers();
	private Deadline deadline = new Deadline();

	/**
	 * The role a verified member holds; without one, members are verified and hold nothing.
	 */
	@JsonProperty("verified-role")
	private @Nullable String verifiedRole;

	/**
	 * The events that start onboarding for a member who is not verified.
	 */
	@Getter
	@Setter
	public static class Triggers {
		/**
		 * A member joins the guild.
		 */
		@JsonProperty("member-join")
		private boolean memberJoin = true;

		/**
		 * Onboarding starts, for every member of the guild.
		 */
		private boolean startup = true;

		/**
		 * A member's profile is cleared ({@code /clear}): they are no longer verified and verify again.
		 */
		@JsonProperty("member-cleared")
		private boolean memberCleared = true;
	}

	/**
	 * How long a member has to finish, and what happens after.
	 */
	@Getter
	@Setter
	public static class Deadline {
		private boolean enabled = true;
		private Duration after = Duration.ofDays(1);
		private DeadlineAction action = DeadlineAction.KICK;
	}
}
