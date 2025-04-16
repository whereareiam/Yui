package me.whereareiam.yue.adapter.command.executor;

import me.whereareiam.yue.api.output.command.Command;
import me.whereareiam.yue.api.output.command.CommandBase;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class MainCommand implements CommandBase {
	@Command(name = "main")
	public void onCommand(SlashCommandInteractionEvent event) {
		event.reply("Main command").queue();
	}
}
