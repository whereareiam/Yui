package me.whereareiam.yue.api.model.config.messages;

import java.util.Map;

public class CommandMessages {
	private MainCommand main;
	private HelpCommand help;

	public static class MainCommand {
		private String description;

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}

	public static class HelpCommand {
		private String description;
		private Map<String, String> variables;

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Map<String, String> getVariables() {
			return variables;
		}

		public void setVariables(Map<String, String> variables) {
			this.variables = variables;
		}
	}

	public MainCommand getMain() {
		return main;
	}

	public void setMain(MainCommand main) {
		this.main = main;
	}

	public HelpCommand getHelp() {
		return help;
	}

	public void setHelp(HelpCommand help) {
		this.help = help;
	}
}
