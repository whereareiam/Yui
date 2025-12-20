package me.whereareiam.yui.adapter.command.executor;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.annotation.ComponentListener;
import me.whereareiam.yui.annotation.command.Argument;
import me.whereareiam.yui.annotation.command.Command;
import me.whereareiam.yui.annotation.command.Definition;
import me.whereareiam.yui.fluctlight.FluctlightService;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.command.Interaction;
import me.whereareiam.yui.util.style.StyleKit;
import me.whereareiam.yui.translation.Translatable;
import me.whereareiam.yui.util.Components;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ClearCommand {
	private final FluctlightService fluctlightService;

	private static final String CONFIRM_LISTENER = "command_clear_confirm";
	private static final String CANCEL_LISTENER = "command_clear_cancel";

	@Definition("clear")
	@Command("clear <user>")
	public void onCommand(Interaction interaction, @Argument("user") User targetUser) {
		Fluctlight fluctlight = interaction.fluctlight();

		// Check if user is trying to clear their own profile
		if (targetUser.getIdLong() == fluctlight.getId()) {
			interaction.replyCallback()
					.replyEmbeds(StyleKit.embeds().error()
							.setTitle(Translatable.of("commands.error.validation.sameUser", fluctlight))
							.build())
					.setEphemeral(true)
					.queue();
			return;
		}

		// Create confirmation embed
		EmbedBuilder embed = StyleKit.embeds().warning();
		embed.setTitle(Translatable.of("commands.clear.confirmation.title", fluctlight));
		embed.setDescription(Translatable.of("commands.clear.confirmation.description", fluctlight));
		embed.addField(
				Translatable.of("commands.clear.confirmation.userInfo", fluctlight),
				String.format("**%s** (`%s`)", targetUser.getAsMention(), targetUser.getId()),
				false
		);

		// Create buttons using the proper Components utility with embedded payload
		var confirmButton = Components.button(ButtonStyle.DANGER, CONFIRM_LISTENER, Translatable.of("vocabulary.confirm", fluctlight), String.valueOf(targetUser.getIdLong()));
		var cancelButton = Components.button(ButtonStyle.SECONDARY, CANCEL_LISTENER, Translatable.of("vocabulary.cancel", fluctlight), String.valueOf(targetUser.getIdLong()));

		// Send the confirmation message with buttons
		interaction.replyCallback()
				.replyEmbeds(embed.build())
				.setEphemeral(true)
				.addActionRow(confirmButton.getButton(), cancelButton.getButton())
				.queue();
	}

	@ComponentListener(CONFIRM_LISTENER)
	public void onConfirmButton(Fluctlight fluctlight, ButtonInteractionEvent event) {
		// Defer the edit to avoid timeout
		event.deferEdit().queue();

		// Get the target user ID from the button payload
		String payload = Components.payload(event);
		if (payload == null) {
			event.getHook().editOriginalEmbeds(StyleKit.embeds().error()
							.setTitle(Translatable.of("commands.error.validation.invalidButton", fluctlight))
							.build())
					.setComponents()
					.queue();
			return;
		}

		long targetUserId = Long.parseLong(payload);
		User targetUser = event.getJDA().getUserById(targetUserId);

		var result = fluctlightService.clear(targetUser.getIdLong());

		if (result.isPresent()) {
			// Success embed
			EmbedBuilder successEmbed = StyleKit.embeds().success();
			successEmbed.setTitle(Translatable.of("commands.clear.success.title", fluctlight));
			successEmbed.setDescription(Translatable.of("commands.clear.success.description", fluctlight));
			successEmbed.addField(
					Translatable.of("commands.clear.success.userInfo", fluctlight),
					String.format("**%s** (`%s`)", targetUser.getAsMention(), targetUser.getId()),
					false
			);

			event.getHook().editOriginalEmbeds(successEmbed.build())
					.setComponents()
					.queue();
		} else {
			// Use predefined error embed with exception message
			event.getHook().editOriginalEmbeds(StyleKit.embeds().error()
					.setTitle(Translatable.of("commands.error.exception", fluctlight))
					.build()).queue();
		}
	}

	@ComponentListener(CANCEL_LISTENER)
	public void onCancelButton(Fluctlight fluctlight, ButtonInteractionEvent event) {
		// Defer the edit to avoid timeout
		event.deferEdit().queue();

		// Show cancellation message
		event.getHook().editOriginalEmbeds(StyleKit.embeds().info()
						.setTitle(Translatable.of("commands.clear.cancelled.title", fluctlight))
						.setDescription(Translatable.of("commands.clear.cancelled.description", fluctlight))
						.build())
				.setComponents()
				.queue();
	}
}
