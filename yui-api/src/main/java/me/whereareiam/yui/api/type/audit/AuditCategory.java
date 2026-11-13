package me.whereareiam.yui.api.type.audit;

/**
 * What an audit entry is about. Whoever writes an entry files it under a category in code; {@code audit.yml} says
 * where each category is posted.
 */
public enum AuditCategory {
	/**
	 * Members joining, leaving and changing.
	 */
	MEMBERS,

	/**
	 * What moderators and Yui's checks do to members.
	 */
	MODERATION,

	/**
	 * Yui itself: updates, builds, plugins.
	 */
	SYSTEM
}
