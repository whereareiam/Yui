package me.whereareiam.yui.onboarding.type;

/**
 * What happens to a member who did not finish onboarding by its deadline.
 */
public enum DeadlineAction {
	/**
	 * The member is kicked, with the reason {@code onboarding.settings.deadline.kick}.
	 */
	KICK,
	/**
	 * Nothing: onboarding ends and the member stays unverified.
	 */
	NOTHING
}
