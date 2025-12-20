package me.whereareiam.yui.adapter.command.executor;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.annotation.command.Command;
import me.whereareiam.yui.annotation.command.Definition;
import org.incendo.cloud.discord.jda6.JDAInteraction;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MainCommand {
	private final HelpCommand helpCommand;

	@Definition("main")
	@Command("main")
	public void onCommand(JDAInteraction interaction) {
		helpCommand.onCommand(interaction, null);
	}
}
