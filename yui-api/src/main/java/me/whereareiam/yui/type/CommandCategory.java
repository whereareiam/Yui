package me.whereareiam.yui.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommandCategory {
	/**
	 * Category for commands that are used for utility purposes.
	 */
	UTILITY("words.category.utility"),

	/**
	 * Category for commands that are used for moderation purposes.
	 */
	MODERATION("words.category.moderation"),

	/**
	 * Category for commands that are used for fun purposes.
	 */
	FUN("words.category.fun"),

	/**
	 * Category for commands that are used for music purposes.
	 */
	ADMINISTRATION("words.category.administration"),

	/**
	 * Category for commands that shouldn't be shown in the help menu.
	 */
	NONE("words.category.none");

	private final String key;
}
