package me.whereareiam.yui.api.model.config.messages;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.api.model.config.messages.command.HelpCommandMessages;
import me.whereareiam.yui.api.model.config.messages.command.MainCommandMessages;

@Getter
@Setter
public class CommandMessages {
	private ErrorMessages error;
	private MainCommandMessages main;
	private HelpCommandMessages help;

	@Getter
	@Setter
	public static class ErrorMessages {
		private String exception;
	}
}
