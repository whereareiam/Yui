package me.whereareiam.yui.adapter.command.executor;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.annotation.Command;
import me.whereareiam.yui.annotation.ComponentListener;
import me.whereareiam.yui.model.PayloadButton;
import me.whereareiam.yui.model.profile.UserProfile;
import me.whereareiam.yui.CommandBase;
import me.whereareiam.yui.service.LanguageService;
import me.whereareiam.yui.service.UserProfileService;
import me.whereareiam.yui.style.StyleKit;
import me.whereareiam.yui.util.Components;
import me.whereareiam.yui.util.EmojiUtil;
import me.whereareiam.yui.translation.Translatable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@AllArgsConstructor
public class LanguageCommand implements CommandBase {
	private final LanguageService languageService;
	private final UserProfileService userProfileService;

	private static final String SELECT_PRIMARY_LISTENER = "command_language_select_primary";
	private static final String SELECT_ADDITIONAL_LISTENER = "command_language_select_additional";
	private static final String CONTINUE_LISTENER = "command_language_continue";
	private static final String CONFIRM_LISTENER = "command_language_confirm";
	private static final String CANCEL_LISTENER = "command_language_cancel";

	@Command(name = "language")
	public void onCommand(SlashCommandInteractionEvent event) {
		long userId = event.getUser().getIdLong();

		Optional<UserProfile> profileOpt = userProfileService.getProfile(userId);
		if (profileOpt.isEmpty())
			return;

		showPrimaryLanguageSelection(event, false);
	}

	@ComponentListener(SELECT_PRIMARY_LISTENER)
	public void onSelectPrimaryLanguage(ButtonInteractionEvent event) {
		String payload = Components.payload(event);
		if (payload == null || payload.isBlank()) {
			event.deferEdit().queue();
			return;
		}

		DiscordLocale selectedLanguage = DiscordLocale.from(payload);
		long userId = event.getUser().getIdLong();

		userProfileService.changePrimaryLanguage(userId, selectedLanguage);
		userProfileService.getProfile(userId).ifPresent(profile -> {
			DiscordLocale[] additionals = profile.getAdditionalLanguages();
			if (additionals != null)
				for (DiscordLocale lang : additionals)
					if (lang != null)
						userProfileService.removeAdditionalLanguage(userId, lang);
		});

		showPrimaryLanguageSelection(event, true);
	}

	@ComponentListener(CONTINUE_LISTENER)
	public void onContinue(ButtonInteractionEvent event) {
		showAdditionalLanguageSelection(event);
	}

	@ComponentListener(SELECT_ADDITIONAL_LISTENER)
	public void onSelectAdditionalLanguage(ButtonInteractionEvent event) {
		String payload = Components.payload(event);
		if (payload == null || payload.isBlank()) {
			event.deferEdit().queue();
			return;
		}

		DiscordLocale selectedLanguage = DiscordLocale.from(payload);
		long userId = event.getUser().getIdLong();
		userProfileService.addAdditionalLanguage(userId, selectedLanguage);

		showAdditionalLanguageSelection(event);
	}

	@ComponentListener(CONFIRM_LISTENER)
	public void onConfirm(ButtonInteractionEvent event) {
		long userId = event.getUser().getIdLong();
		Optional<UserProfile> profileOpt = userProfileService.getProfile(userId);

		if (profileOpt.isEmpty()) {
			event.deferEdit().queue();
			return;
		}

		EmbedBuilder embed = StyleKit.embeds().success().setTitle(Translatable.of("commands.language.success.title", userId));
		event.editMessageEmbeds(embed.build())
				.setComponents()
				.queue();
	}

	@ComponentListener(CANCEL_LISTENER)
	public void onCancel(ButtonInteractionEvent event) {
		event.editMessageEmbeds(StyleKit.embeds().info()
						.setTitle(Translatable.of("commands.language.cancelled.title", event.getUser().getIdLong()))
						.build())
				.setComponents()
				.queue();
	}

	private void showPrimaryLanguageSelection(IReplyCallback event, boolean postSelection) {
		long userId = event.getUser().getIdLong();

		Optional<UserProfile> profileOpt = userProfileService.getProfile(userId);
		if (profileOpt.isEmpty()) {
			if (event instanceof ButtonInteractionEvent bie)
				bie.deferEdit().queue();
			return;
		}

		UserProfile profile = profileOpt.get();

		EmbedBuilder embed = StyleKit.embeds().primary()
				.setTitle(Translatable.of("commands.language.primary.title", userId))
				.setDescription(Translatable.of("commands.language.primary.description", userId));

		// Exclude ONLY the current primary (ignore additional languages here)
		Set<DiscordLocale> excluded = new HashSet<>();
		if (profile.getPrimaryLanguage() != null)
			excluded.add(profile.getPrimaryLanguage());

		List<Button> languageButtons = buildLanguageButtons(
				languageService.getAvailableLanguages(),
				excluded,
				SELECT_PRIMARY_LISTENER
		);

		// Footer controls:
		List<Button> footer = new ArrayList<>();
		if (postSelection) {
			footer.add(Components.button(
					ButtonStyle.SUCCESS,
					CONTINUE_LISTENER,
					Translatable.of("vocabulary.proceed", userId)
			));
			footer.add(Components.button(
					ButtonStyle.SECONDARY,
					CONFIRM_LISTENER,
					Translatable.of("vocabulary.confirm", userId)
			));
		} else {
			footer.add(Components.button(
					ButtonStyle.SECONDARY,
					CANCEL_LISTENER,
					Translatable.of("vocabulary.cancel", userId)
			));
		}

		List<ActionRow> rows = toActionRows(languageButtons, footer);

		replyOrEdit(event, embed, rows);
	}

	private void showAdditionalLanguageSelection(IReplyCallback event) {
		long userId = event.getUser().getIdLong();

		Optional<UserProfile> profileOpt = userProfileService.getProfile(userId);
		if (profileOpt.isEmpty()) {
			if (event instanceof ButtonInteractionEvent bie) {
				bie.deferEdit().queue();
			}
			return;
		}

		UserProfile profile = profileOpt.get();

		EmbedBuilder embed = StyleKit.embeds().primary()
				.setTitle(Translatable.of("commands.language.additional.title", userId))
				.setDescription(Translatable.of("commands.language.additional.description", userId));

		Set<DiscordLocale> currentAdditionals = profile.getAdditionalLanguages() != null
				? new HashSet<>(Arrays.asList(profile.getAdditionalLanguages()))
				: new HashSet<>();

		// Exclude primary + already-selected additionals
		Set<DiscordLocale> excluded = new HashSet<>(currentAdditionals);
		if (profile.getPrimaryLanguage() != null)
			excluded.add(profile.getPrimaryLanguage());

		List<Button> languageButtons = buildLanguageButtons(
				languageService.getAvailableLanguages(),
				excluded,
				SELECT_ADDITIONAL_LISTENER
		);

		// Footer controls: show Confirm if we have any additionals, otherwise Cancel
		List<Button> footer = new ArrayList<>();
		if (!currentAdditionals.isEmpty()) {
			footer.add(Components.button(
					ButtonStyle.SECONDARY,
					CONFIRM_LISTENER,
					Translatable.of("vocabulary.confirm", userId)
			));
		} else {
			footer.add(Components.button(
					ButtonStyle.SECONDARY,
					CANCEL_LISTENER,
					Translatable.of("vocabulary.cancel", userId)
			));
		}

		List<ActionRow> rows = toActionRows(languageButtons, footer);

		replyOrEdit(event, embed, rows);
	}

	private List<Button> buildLanguageButtons(Collection<DiscordLocale> all, Set<DiscordLocale> excluded, String listenerId) {
		if (all == null || all.isEmpty()) return Collections.emptyList();

		return all.stream()
				.filter(Objects::nonNull)
				.filter(lang -> excluded == null || !excluded.contains(lang))
				.map(lang -> Components.button(
						ButtonStyle.SECONDARY,
						listenerId,
						EmojiUtil.of(lang),
						lang.getLocale()
				))
				.map(PayloadButton::getButton)
				.toList();
	}

	private List<ActionRow> toActionRows(List<Button> mainButtons, List<Button> footerButtons) {
		List<ActionRow> rows = new ArrayList<>();
		List<Button> working = new ArrayList<>(mainButtons == null ? List.of() : mainButtons);

		final int perRow = 5;

		for (int i = 0; i < working.size(); i += perRow) {
			int end = Math.min(i + perRow, working.size());
			rows.add(ActionRow.of(working.subList(i, end)));
		}

		if (footerButtons != null && !footerButtons.isEmpty()) {
			if (!rows.isEmpty()) {
				// Try to place footer into last row if there is capacity
				List<Button> lastRowButtons = rows.getLast().getButtons();
				int lastRowSize = lastRowButtons.size();
				int remaining = perRow - lastRowSize;

				if (remaining > 0) {
					List<Button> toPlace = footerButtons.subList(0, Math.min(remaining, footerButtons.size()));
					List<Button> combined = new ArrayList<>(lastRowButtons);
					combined.addAll(toPlace);
					rows.set(rows.size() - 1, ActionRow.of(combined));
					footerButtons = footerButtons.subList(toPlace.size(), footerButtons.size());
				}
			}

			for (int i = 0; i < footerButtons.size(); i += perRow) {
				int end = Math.min(i + perRow, footerButtons.size());
				rows.add(ActionRow.of(footerButtons.subList(i, end)));
			}
		}

		return rows;
	}

	private void replyOrEdit(IReplyCallback event, EmbedBuilder embed, List<ActionRow> rows) {
		List<ActionRow> components = (rows == null) ? List.of() : rows;

		if (event instanceof ButtonInteractionEvent bie) {
			bie.editMessageEmbeds(embed.build())
					.setComponents(components)
					.queue();
			return;
		}

		if (event instanceof SlashCommandInteractionEvent sce) {
			sce.replyEmbeds(embed.build())
					.setEphemeral(true)
					.setComponents(components)
					.queue();
			return;
		}

		event.replyEmbeds(embed.build())
				.setEphemeral(true)
				.setComponents(components)
				.queue();
	}
}
