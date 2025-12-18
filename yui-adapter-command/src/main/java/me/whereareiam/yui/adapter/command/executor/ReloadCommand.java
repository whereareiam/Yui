package me.whereareiam.yui.adapter.command.executor;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yui.annotation.ComponentListener;
import me.whereareiam.yui.annotation.command.Command;
import me.whereareiam.yui.annotation.command.Definition;
import me.whereareiam.yui.registry.Registry;
import me.whereareiam.yui.style.StyleKit;
import me.whereareiam.yui.translation.Translatable;
import me.whereareiam.yui.util.Components;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.incendo.cloud.discord.jda6.JDAInteraction;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Reloads the component's configuration and state.
 * This method is called when the component's configuration
 * need to be reinitialized during runtime.
 * <p>
 * The reload command performs a comprehensive system reload by iterating
 * through all services registered with the ReloadableProvider. This ensures:
 * - Services are reloaded in the same order they were registered
 * - All services implement the Reloadable interface with proper
 * shutdown/cleanup before reinitialization
 * - Clean state management without resource leaks
 */
@Slf4j
@Component
@AllArgsConstructor
public class ReloadCommand {
	private final Registry<Reloadable> reloadableRegistry;

	private static final String CONFIRM_LISTENER = "command_reload_confirm";
	private static final String CANCEL_LISTENER = "command_reload_cancel";

	@Definition("reload")
	@Command("reload")
	public void onCommand(JDAInteraction interaction) {
		long userId = interaction.user().getIdLong();

		// Create confirmation embed
		EmbedBuilder embed = StyleKit.embeds().warning();
		embed.setTitle(Translatable.of("commands.reload.confirmation.title", userId));
		embed.setDescription(Translatable.of("commands.reload.confirmation.description", userId));

		// Create buttons
		var confirmButton = Components.button(ButtonStyle.DANGER, CONFIRM_LISTENER, Translatable.of("vocabulary.confirm", userId));
		var cancelButton = Components.button(ButtonStyle.SECONDARY, CANCEL_LISTENER, Translatable.of("vocabulary.cancel", userId));

		// Send the confirmation message with buttons
		interaction.replyCallback()
				.replyEmbeds(embed.build())
				.setEphemeral(true)
				.addActionRow(confirmButton, cancelButton)
				.queue();
	}

	@ComponentListener(CONFIRM_LISTENER)
	public void onConfirmButton(ButtonInteractionEvent event) {
		// Defer the edit to avoid timeout
		event.deferEdit().queue();

		// Start reload process
		CompletableFuture.runAsync(() -> performReload(event))
				.exceptionally(throwable -> {
					log.error("Error during reload process", throwable);

					event.getHook().editOriginalEmbeds(StyleKit.embeds().error()
									.setTitle(Translatable.of("commands.reload.error.title", event.getUser().getIdLong()))
									.setDescription(Translatable.of("commands.reload.error.description", event.getUser().getIdLong()))
									.build())
							.setComponents()
							.queue();
					return null;
				});
	}

	private void performReload(ButtonInteractionEvent event) {
		try {
			log.info("");
			log.info("Reloading components...");

			// Reload all reloadable services in registration order
			log.debug("Reloading all reloadable services...");
			reloadableRegistry.getAll().forEach(reloadable -> {
				try {
					log.debug("Reloading service: {}", reloadable.getClass().getSimpleName());
					reloadable.reload();
					log.debug("Successfully reloaded service: {}", reloadable.getClass().getSimpleName());
				} catch (Exception e) {
					log.error("Failed to reload service: {}", reloadable.getClass().getSimpleName(), e);
				}
			});

			log.info("Reload completed successfully.");
			log.info("");

			// Success embed
			EmbedBuilder successEmbed = StyleKit.embeds().success();
			successEmbed.setTitle(Translatable.of("commands.reload.success.title", event.getUser().getIdLong()));
			successEmbed.setDescription(Translatable.of("commands.reload.success.description", event.getUser().getIdLong()));

			// Remove buttons and show success message
			event.getHook()
					.editOriginalEmbeds(successEmbed.build())
					.setComponents()
					.queue();

		} catch (Exception e) {
			log.error("Error during reload process", e);
			event.getHook().editOriginalEmbeds(StyleKit.embeds().error()
							.setTitle(Translatable.of("commands.reload.error.title", event.getUser().getIdLong()))
							.build())
					.setComponents()
					.queue();
		}
	}

	@ComponentListener(CANCEL_LISTENER)
	public void onCancelButton(ButtonInteractionEvent event) {
		// Defer the edit to avoid timeout
		event.deferEdit().queue();

		// Show cancellation message and remove buttons
		event.getHook().editOriginalEmbeds(StyleKit.embeds().info()
						.setTitle(Translatable.of("commands.reload.cancelled.title", event.getUser().getIdLong()))
						.setDescription(Translatable.of("commands.reload.cancelled.description", event.getUser().getIdLong()))
						.build())
				.setComponents()
				.queue();
	}
}
