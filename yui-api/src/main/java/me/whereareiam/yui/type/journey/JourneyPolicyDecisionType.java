package me.whereareiam.yui.type.journey;

/**
 * Decision type returned by journey policy evaluation.
 */
public enum JourneyPolicyDecisionType {
	/**
	 * Policy allows normal execution.
	 */
	ALLOW,
	/**
	 * Policy skips current scope and continues with the next one.
	 */
	SKIP,
	/**
	 * Policy redirects execution to a different step id.
	 */
	REDIRECT,
	/**
	 * Policy rejects execution and fails the journey.
	 */
	REJECT
}
