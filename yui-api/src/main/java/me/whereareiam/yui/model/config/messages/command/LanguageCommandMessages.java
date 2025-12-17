package me.whereareiam.yui.model.config.messages.command;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LanguageCommandMessages {
	private String description;
	private String example;

	private Primary primary;
	private Additional additional;
	private Success success;
	private Cancelled cancelled;

	@Getter
	@Setter
	public static class Primary {
		private String title;
		private List<String> description;
	}

	@Getter
	@Setter
	public static class Additional {
		private String title;
		private List<String> description;
	}

	@Getter
	@Setter
	public static class Success {
		private String title;
	}

	@Getter
	@Setter
	public static class Cancelled {
		private String title;
	}
}
