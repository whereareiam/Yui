package me.whereareiam.yui.verification.type;

/**
 * What happens to a member who did not finish verification by its deadline.
 */
public enum DeadlineAction {
	/**
	 * The member is kicked, with the reason {@code verification.settings.deadline.kick}.
	 */
	KICK,
	/**
	 * Nothing: the verification ends and the member stays unverified.
	 */
	NOTHING
}
