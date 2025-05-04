package me.whereareiam.yui.api.type;

public enum CommandCategory {
	/**
	 * Category for commands that are used for utility purposes.
	 */
	UTILITY("vocabulary.category.utility"),

	/**
	 * Category for commands that are used for moderation purposes.
	 */
	MODERATION("vocabulary.category.moderation"),

	/**
	 * Category for commands that are used for fun purposes.
	 */
	FUN("vocabulary.category.fun"),

	/**
	 * Category for commands that are used for music purposes.
	 */
	ADMINISTRATION("vocabulary.category.administration"),

	/**
	 * Category for commands that shouldn't be shown in the help menu.
	 */
	NONE("vocabulary.category.none");

	private final String key;

	CommandCategory(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
