package me.whereareiam.yui.adapter.config.template.messages.command;

import me.whereareiam.yui.model.config.messages.command.MainCommandMessages;
import me.whereareiam.yui.config.DefaultConfig;
import org.springframework.stereotype.Component;

@Component
public class MainCommandMessagesTemplate implements DefaultConfig<MainCommandMessages> {
	@Override
	public MainCommandMessages getDefault() {
		MainCommandMessages main = new MainCommandMessages();
		main.setDescription("The main command for the bot. In most cases, it is used as a prefix for other commands.");
		main.setExample("/yui");
		return main;
	}
}


