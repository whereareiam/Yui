package me.whereareiam.yui.adapter.command.executor;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.api.annotation.Command;
import me.whereareiam.yui.api.annotation.ComponentListener;
import me.whereareiam.yui.api.output.CommandBase;
import me.whereareiam.yui.api.output.service.ProfileManagementService;
import me.whereareiam.yui.api.style.StyleKit;
import me.whereareiam.yui.api.util.Components;
import me.whereareiam.yui.api.util.Translatable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ClearCommand implements CommandBase {
	private final ProfileManagementService profileManagementService;

	@Command(name = "clear")
	public void onCommand(SlashCommandInteractionEvent event) {
		OptionMapping userOption = event.getOption("user");
		if (userOption == null) {
			event.replyEmbeds(StyleKit.embeds().error()
							.setTitle(Translatable.of("commands.error.validation.userRequired", event.getUser().getIdLong()))
							.build())
					.setEphemeral(true)
					.queue();
			return;
		}

		net.dv8tion.jda.api.entities.User targetUser = userOption.getAsUser();

		// Check if user is trying to clear their own profile
		if (targetUser.getIdLong() == event.getUser().getIdLong()) {
			event.replyEmbeds(StyleKit.embeds().error()
							.setTitle(Translatable.of("commands.error.validation.sameUser", event.getUser().getIdLong()))
							.build())
					.setEphemeral(true)
					.queue();
			return;
		}

		// Create confirmation embed
		EmbedBuilder embed = StyleKit.embeds().warning();
		embed.setTitle(Translatable.of("commands.clear.confirmation.title", event.getUser().getIdLong()));
		embed.setDescription(Translatable.of("commands.clear.confirmation.description", event.getUser().getIdLong()));
		embed.addField(
				Translatable.of("commands.clear.confirmation.userInfo", event.getUser().getIdLong()),
				String.format("**%s** (`%s`)", targetUser.getAsMention(), targetUser.getId()),
				false
		);

		// Create buttons using the proper Components utility with embedded payload
		var confirmButton = Components.button(ButtonStyle.DANGER, "clear_confirm", Translatable.of("vocabulary.confirm", event.getUser().getIdLong()), String.valueOf(targetUser.getIdLong()));
		var cancelButton = Components.button(ButtonStyle.SECONDARY, "clear_cancel", Translatable.of("vocabulary.cancel", event.getUser().getIdLong()), String.valueOf(targetUser.getIdLong()));

		// Send the confirmation message with buttons
		event.replyEmbeds(embed.build())
				.setEphemeral(true)
				.addActionRow(confirmButton.getButton(), cancelButton.getButton())
				.queue();
	}

	@ComponentListener("clear_confirm")
	public void onConfirmButton(ButtonInteractionEvent event) {
		// Defer the edit to avoid timeout
		event.deferEdit().queue();

		// Get the target user ID from the button payload
		String payload = Components.payload(event);
		if (payload == null) {
			event.getHook().editOriginalEmbeds(StyleKit.embeds().error()
							.setTitle(Translatable.of("commands.error.validation.invalidButton", event.getUser().getIdLong()))
							.build())
					.setComponents()
					.queue();
			return;
		}

		long targetUserId = Long.parseLong(payload);
		net.dv8tion.jda.api.entities.User targetUser = event.getJDA().getUserById(targetUserId);

		var result = profileManagementService.clearAndReinitializeProfile(targetUser.getIdLong());

		if (result.isPresent()) {
			// Success embed
			EmbedBuilder successEmbed = StyleKit.embeds().success();
			successEmbed.setTitle(Translatable.of("commands.clear.success.title", event.getUser().getIdLong()));
			successEmbed.setDescription(Translatable.of("commands.clear.success.description", event.getUser().getIdLong()));
			successEmbed.addField(
					Translatable.of("commands.clear.success.userInfo", event.getUser().getIdLong()),
					String.format("**%s** (`%s`)", targetUser.getAsMention(), targetUser.getId()),
					false
			);

			event.getHook().editOriginalEmbeds(successEmbed.build())
					.setComponents()
					.queue();
		} else {
			// Use predefined error embed with exception message
			event.getHook().editOriginalEmbeds(StyleKit.embeds().error()
					.setTitle(Translatable.of("commands.error.exception", event.getUser().getIdLong()))
					.build()).queue();
		}
	}

	@ComponentListener("clear_cancel")
	public void onCancelButton(ButtonInteractionEvent event) {
		// Defer the edit to avoid timeout
		event.deferEdit().queue();

		// Show cancellation message
		event.getHook().editOriginalEmbeds(StyleKit.embeds().info()
						.setTitle(Translatable.of("commands.clear.cancelled.title", event.getUser().getIdLong()))
						.setDescription(Translatable.of("commands.clear.cancelled.description", event.getUser().getIdLong()))
						.build())
				.setComponents()
				.queue();
	}
}
