package me.whereareiam.yue.api.output;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface CommandBase {
	void onCommand(SlashCommandInteractionEvent event);
}
