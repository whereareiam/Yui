package me.whereareiam.yui.model.config.messages;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VocabularyMessages {
	private String cancel;
	private String proceed;
	private String confirm;
	private String back;
	private String next;
	private String yes;
	private String no;

	private Category category;

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
