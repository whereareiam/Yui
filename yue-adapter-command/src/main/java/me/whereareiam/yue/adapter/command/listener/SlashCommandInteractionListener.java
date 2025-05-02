package me.whereareiam.yue.adapter.command.listener;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yue.adapter.command.cooldown.CooldownService;
import me.whereareiam.yue.adapter.command.registry.CommandDefinition;
import me.whereareiam.yue.adapter.command.registry.CommandRegistry;
import me.whereareiam.yue.api.style.StyleKit;
import me.whereareiam.yue.api.util.Translatable;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

/**
 * Listens for slash command interactions and delegates to the appropriate handler method
 * stored in CommandDefinition. Also handles cooldowns if configured.
 */
@Slf4j
@Component
@AllArgsConstructor
public class SlashCommandInteractionListener extends ListenerAdapter {
	private final CommandRegistry registry;
	private final CooldownService cooldownService;

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		// If there's a subcommand, treat that as the command name.
		String commandName = event.getSubcommandName() != null
				? event.getSubcommandName()
				: event.getName();

		CommandDefinition definition = registry.get(commandName);
		if (definition == null)
			return;

		// If the command has a cooldown, handle it
		if (cooldownService.handleCooldown(event, definition.getCommandConfig().getCooldown()))
			return;

		try {
			definition.invoke(event);
		} catch (Exception ex) {
			log.error("Exception while executing slash command '{}': ", commandName, ex);
			event.replyEmbeds(StyleKit.embeds().error()
							.setTitle(Translatable.of("commands.error.exception", event.getUser().getIdLong()))
							.build())
					.setEphemeral(true)
					.queue();
		}
	}
}
