package me.whereareiam.yui.common.config.template.messages.command;

import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.yui.model.config.messages.command.MainCommandMessages;
import org.springframework.stereotype.Component;

@Component
public class MainCommandMessagesTemplate implements TemplateProvider<MainCommandMessages> {
	@Override
	public MainCommandMessages supply(MainCommandMessages main) {
		main.setDescription("The main command for the bot. In most cases, it is used as a prefix for other commands.");
		main.setExample("/yui");
		return main;
	}
}


