package me.whereareiam.yui.adapter.command.executor;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.annotation.Command;
import me.whereareiam.yui.CommandBase;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MainCommand implements CommandBase {
	private final HelpCommand helpCommand;

	@Command(name = "main")
	public void onCommand(SlashCommandInteractionEvent event) {
		helpCommand.onCommand(event);
	}
}
