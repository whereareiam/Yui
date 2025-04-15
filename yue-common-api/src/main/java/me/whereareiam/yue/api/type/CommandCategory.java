package me.whereareiam.yue.api.type;

public enum CommandCategory {
	/**
	 * Category for commands that are used for utility purposes.
	 */
	UTILITY,

	/**
	 * Category for commands that are used for moderation purposes.
	 */
	MODERATION,

	/**
	 * Category for commands that are used for fun purposes.
	 */
	FUN,

	/**
	 * Category for commands that are used for music purposes.
	 */
	ADMIN,
	
	/**
	 * Category for commands that shouldn't be shown in the help menu.
	 */
	NONE
}
