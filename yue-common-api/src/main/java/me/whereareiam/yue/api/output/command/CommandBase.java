package me.whereareiam.yue.api.output.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface CommandBase {
	void onCommand(SlashCommandInteractionEvent event);
}
