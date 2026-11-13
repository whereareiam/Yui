package me.whereareiam.yui.api.model.config.messages;

import lombok.Getter;
import lombok.Setter;

/**
 * {@code messages/<locale>/words.yml}: words the other texts and Yui's buttons share, such as {@code {@words.cancel}}.
 */
@Getter
@Setter
public class Words {
	private String cancel;
	private String proceed;
	private String confirm;
	private String edit;
	private String back;
	private String next;
	private String yes;
	private String no;
	private Category category;

	/**
	 * The names of the command categories.
	 */
	@Getter
	@Setter
	public static class Category {
		private String utility;
		private String fun;
		private String moderation;
		private String administration;
		private String none;
	}
}
