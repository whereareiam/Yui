package me.whereareiam.yui.adapter.command.listener;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.adapter.command.cooldown.CooldownService;
import me.whereareiam.yui.adapter.command.registry.CommandDefinition;
import me.whereareiam.yui.adapter.command.registry.CommandRegistry;
import me.whereareiam.yui.adapter.command.requirements.CommandRequirementErrorService;
import me.whereareiam.yui.adapter.command.requirements.CommandRequirementEvaluatorConfig;
import me.whereareiam.yui.model.profile.UserProfile;
import me.whereareiam.yui.model.requirement.RequirementContext;
import me.whereareiam.yui.requirement.RequirementEvaluator;
import me.whereareiam.yui.service.UserProfileService;
import me.whereareiam.yui.style.StyleKit;
import me.whereareiam.yui.translation.Translatable;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
	private final RequirementEvaluator requirementEngine;
	private final CommandRequirementErrorService requirementErrorService;
	private final CommandRequirementEvaluatorConfig requirementConfig;
	private final UserProfileService userProfileService;

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

		// Get or create UserProfile for requirement evaluation
		Optional<UserProfile> userProfileOpt = userProfileService.getProfile(event.getUser().getIdLong());
		UserProfile userProfile = userProfileOpt.orElseGet(() -> {
			// Create profile if it doesn't exist
			Optional<UserProfile> newProfile = userProfileService.createProfile(event.getUser().getIdLong());
			return newProfile.orElseThrow(() -> new IllegalStateException("Failed to create user profile"));
		});

		// Create RequirementContext with UserProfile
		RequirementContext requirementContext = new RequirementContext(event, userProfile);

		// Evaluate requirements if present using command-specific configuration
		if (!requirementEngine.evaluate(requirementContext, definition.getCommandConfig().getRequirements(), requirementConfig)) {
			// Send error message to user explaining why the command failed
			String errorMessage = requirementErrorService.generateErrorMessage(
				definition.getCommandConfig().getRequirements(), 
				event.getUser().getIdLong()
			);
			
			event.replyEmbeds(StyleKit.embeds().error()
					.setTitle(Translatable.of("commands.error.requirement.title", event.getUser().getIdLong()))
					.setDescription(errorMessage)
					.build())
					.setEphemeral(true)
					.queue();
			return;
		}

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
