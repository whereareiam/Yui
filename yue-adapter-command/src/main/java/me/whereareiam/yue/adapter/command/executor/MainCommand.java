package me.whereareiam.yue.adapter.command.executor;

import lombok.AllArgsConstructor;
import me.whereareiam.yue.api.output.command.Command;
import me.whereareiam.yue.api.output.command.CommandBase;
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
