package me.whereareiam.yui.model.config.messages.command;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StatusCommandMessages {
	private String description;
	private String example;
	private Overview overview;

	@Getter
	@Setter
	public static class Overview {
		private String title;
		private List<String> description;
		private Plugins plugins;
		private Fields fields;
	}

	@Getter
	@Setter
	public static class Plugins {
		private String title;
		private String empty;
		private String format;
		private Status status;

		@Getter
		@Setter
		public static class Status {
			private String enabled;
			private String disabled;
			private String loadable;
		}
	}

	@Getter
	@Setter
	public static class Fields {
		private Memory memory;
		private Cpu cpu;
		private Java java;
		private Os os;
		private Locale locale;

		@Getter
		@Setter
		public static class Memory {
			private String title;
			private List<String> value;
		}

		@Getter
		@Setter
		public static class Cpu {
			private String title;
			private List<String> value;
		}

		@Getter
		@Setter
		public static class Java {
			private String title;
			private List<String> value;
		}

		@Getter
		@Setter
		public static class Os {
			private String title;
			private List<String> value;
		}

		@Getter
		@Setter
		public static class Locale {
			private String title;
			private List<String> value;
		}
	}
}
