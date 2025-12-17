package me.whereareiam.yui.model.config.messages.command;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClearCommandMessages {
	private String description;
	private String example;
	private Confirmation confirmation;
	private Success success;
	private Cancelled cancelled;

	@Getter
	@Setter
	public static class Confirmation {
		private String title;
		private List<String> description;
		private String userInfo;
	}

	@Getter
	@Setter
	public static class Success {
		private String title;
		private List<String> description;
		private String userInfo;
	}

	@Getter
	@Setter
	public static class Cancelled {
		private String title;
		private List<String> description;
	}
}
