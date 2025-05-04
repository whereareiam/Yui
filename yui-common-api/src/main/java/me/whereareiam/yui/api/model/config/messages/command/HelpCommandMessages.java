package me.whereareiam.yui.api.model.config.messages.command;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class HelpCommandMessages {
	private String description;
	private String example;
	private Map<String, String> variables;
	private Information information;
	private Category category;

	@Getter
	@Setter
	public static class Information {
		private Global global;
		private Specific specific;

		@Getter
		@Setter
		public static class Global {
			private String title;
			private String description;
		}

		@Getter
		@Setter
		public static class Specific {
			private String title;
			private String description;
			private String headFormat;
			private String footFormat;
		}
	}

	@Getter
	@Setter
	public static class Category {
		private String utility;
		private String fun;
		private String moderation;
		private String administration;
	}
}