package me.whereareiam.yui.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
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
}
