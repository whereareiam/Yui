package me.whereareiam.yui.api.model.config.messages.command;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class PluginCommandMessages {
	private String description;
	private String example;
	private Map<String, String> variables;

	private Main main;
	private Controls controls;
	private Category enable;
	private Category disable;
	private Category load;
	private Category unload;
	private Action action;

	@Getter
	@Setter
	public static class Main {
		private String title;
		private String description;
		private String format;
		private Fields fields;

		@Getter
		@Setter
		public static class Fields {
			private String enabled;
			private String disabled;
			private String loadable;
		}
	}

	@Getter
	@Setter
	public static class Controls {
		private String enable;
		private String disable;
		private String load;
		private String unload;
		private String reload;
	}

	@Getter
	@Setter
	public static class Category {
		private String title;
		private String description;
		private String empty;
		private String format;
	}

	@Getter
	@Setter
	public static class Action {
		private String errorTitle;
	}
}


