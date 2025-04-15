package me.whereareiam.yue.adapter.command.listener;

import me.whereareiam.yue.adapter.command.cooldown.CooldownService;
import me.whereareiam.yue.adapter.command.registry.CommandRegistry;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class SlashCommandInteractionListener extends ListenerAdapter {
	private final CommandRegistry registry;
	private final CooldownService cooldownService;

	public SlashCommandInteractionListener(CommandRegistry registry,
	                                       CooldownService cooldownService) {

		this.registry = registry;
		this.cooldownService = cooldownService;
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		String commandName = event.getName();

		if (event.getSubcommandName() != null)
			commandName = event.getSubcommandName();

		var commandDefinition = registry.get(commandName);

		if (commandDefinition == null)
			return;

		if (cooldownService.handleCooldown(event, commandDefinition.getCommandConfig().getCooldown()))
			return;

		try {
			commandDefinition.invoke(event);
		} catch (Exception e) {
			event.reply("An error occurred while executing this command.").setEphemeral(true).queue();
			e.printStackTrace();
		}
	}
}