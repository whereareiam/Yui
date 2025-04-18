package me.whereareiam.yue.api.model.config.messages;

import me.whereareiam.yue.api.model.config.messages.command.HelpCommandMessages;
import me.whereareiam.yue.api.model.config.messages.command.MainCommandMessages;

public class CommandMessages {
	private MainCommandMessages main;
	private HelpCommandMessages help;

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
