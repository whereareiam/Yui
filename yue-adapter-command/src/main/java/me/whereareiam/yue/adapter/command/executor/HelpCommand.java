package me.whereareiam.yue.adapter.command.executor;

import me.whereareiam.yue.api.output.command.Command;
import me.whereareiam.yue.api.output.command.CommandBase;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand implements CommandBase {
	@Command(name = "help")
	public void onCommand(SlashCommandInteractionEvent event) {
		event.reply("Help Command").queue();
	}
}
