package me.whereareiam.yue.api.model.config.messages;

import me.whereareiam.yue.api.model.config.messages.command.HelpCommandMessages;
import me.whereareiam.yue.api.model.config.messages.command.MainCommandMessages;

public class CommandMessages {
	private ErrorMessages error;
	private MainCommandMessages main;
	private HelpCommandMessages help;

	public static class ErrorMessages {
		private String exception;

		public String getException() {
			return exception;
		}

		public void setException(String exception) {
			this.exception = exception;
		}
	}

	public ErrorMessages getError() {
		return error;
	}

	public void setError(ErrorMessages error) {
		this.error = error;
	}

	public MainCommandMessages getMain() {
		return main;
	}

	public void setMain(MainCommandMessages main) {
		this.main = main;
	}

	public HelpCommandMessages getHelp() {
		return help;
	}

	public void setHelp(HelpCommandMessages help) {
		this.help = help;
	}
}
