package me.whereareiam.yui.adapter.command.executor;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.annotation.command.Command;
import me.whereareiam.yui.annotation.command.Definition;
import me.whereareiam.yui.command.Interaction;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MainCommand {
	private final HelpCommand helpCommand;

	@Definition("main")
	@Command("main")
	public void onCommand(Interaction interaction) {
		helpCommand.onCommand(interaction, null);
	}
}
