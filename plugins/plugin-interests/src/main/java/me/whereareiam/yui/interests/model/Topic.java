package me.whereareiam.yui.interests.model;

import lombok.Getter;
import lombok.Setter;

/**
 * One topic of {@code topics.yml}; its name and description are texts of {@code messages/<locale>/topics.yml}.
 */
@Getter
@Setter
public class Topic {
	/**
	 * The role a member who picked the topic holds.
	 */
	private String role;
}
