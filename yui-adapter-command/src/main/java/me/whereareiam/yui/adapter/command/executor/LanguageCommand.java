package me.whereareiam.yui.adapter.command.executor;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.annotation.ComponentListener;
import me.whereareiam.yui.annotation.command.Command;
import me.whereareiam.yui.annotation.command.Definition;
import me.whereareiam.yui.command.Interaction;
import me.whereareiam.yui.model.PayloadButton;
import me.whereareiam.yui.model.config.languages.LanguageEntry;
import me.whereareiam.yui.model.config.languages.Languages;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.persistence.LanguagePersistence;
import me.whereareiam.yui.util.style.StyleKit;
import me.whereareiam.yui.util.translation.Translatable;
import me.whereareiam.yui.util.Components;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@AllArgsConstructor
public class LanguageCommand {
	private final LanguagePersistence languagePersistence;
	private final ObjectProvider<Languages> languagesProvider;

	private static final String SELECT_PRIMARY_LISTENER = "command_language_select_primary";
	private static final String SELECT_ADDITIONAL_LISTENER = "command_language_select_additional";
	private static final String CONTINUE_LISTENER = "command_language_continue";
	private static final String CONFIRM_LISTENER = "command_language_confirm";
	private static final String CANCEL_LISTENER = "command_language_cancel";

	@Definition("language")
	@Command("language")
	public void onCommand(Interaction interaction) {
		showPrimaryLanguageSelection(interaction.replyCallback(), interaction.fluctlight(), false);
	}

	@ComponentListener(SELECT_PRIMARY_LISTENER)
	public void onSelectPrimaryLanguage(Fluctlight fluctlight, ButtonInteractionEvent event) {
		String payload = Components.payload(event);
		if (payload == null || payload.isBlank()) {
			event.deferEdit().queue();
			return;
		}

		DiscordLocale selectedLanguage = DiscordLocale.from(payload);

		// Remove all additional languages before changing primary so we don't drop the new primary role
		DiscordLocale[] additionals = fluctlight.getAdditionalLanguages();
		if (additionals != null)
			for (DiscordLocale lang : additionals)
				if (lang != null)
					fluctlight.removeAdditionalLanguage(lang);

		fluctlight.setPrimaryLanguage(selectedLanguage);

		showPrimaryLanguageSelection(event, fluctlight, true);
	}

	@ComponentListener(CONTINUE_LISTENER)
	public void onContinue(Fluctlight fluctlight, ButtonInteractionEvent event) {
		showAdditionalLanguageSelection(event, fluctlight);
	}

	@ComponentListener(SELECT_ADDITIONAL_LISTENER)
	public void onSelectAdditionalLanguage(Fluctlight fluctlight, ButtonInteractionEvent event) {
		String payload = Components.payload(event);
		if (payload == null || payload.isBlank()) {
			event.deferEdit().queue();
			return;
		}

		DiscordLocale selectedLanguage = DiscordLocale.from(payload);
		fluctlight.addAdditionalLanguage(selectedLanguage);

		showAdditionalLanguageSelection(event, fluctlight);
	}

	@ComponentListener(CONFIRM_LISTENER)
	public void onConfirm(Fluctlight fluctlight, ButtonInteractionEvent event) {
		EmbedBuilder embed = StyleKit.embeds().success().setTitle(Translatable.text("commands.language.success.title").resolve(fluctlight));
		event.editMessageEmbeds(embed.build())
				.setComponents()
				.queue();
	}

	@ComponentListener(CANCEL_LISTENER)
	public void onCancel(Fluctlight fluctlight, ButtonInteractionEvent event) {
		event.editMessageEmbeds(StyleKit.embeds().info()
						.setTitle(Translatable.text("commands.language.cancelled.title").resolve(fluctlight))
						.build())
				.setComponents()
				.queue();
	}

	private void showPrimaryLanguageSelection(IReplyCallback event, Fluctlight fluctlight, boolean postSelection) {
		EmbedBuilder embed = StyleKit.embeds().primary()
				.setTitle(Translatable.text("commands.language.primary.title").resolve(fluctlight))
				.setDescription(Translatable.text("commands.language.primary.description").resolve(fluctlight));

		// Exclude ONLY the current primary (ignore additional languages here)
		Set<DiscordLocale> excluded = new HashSet<>();
		if (fluctlight.getPrimaryLanguage() != null)
			excluded.add(fluctlight.getPrimaryLanguage());

		List<Button> languageButtons = buildLanguageButtons(
				languagePersistence.getAvailableLanguages(),
				excluded,
				SELECT_PRIMARY_LISTENER
		);

		// Footer controls:
		List<Button> footer = new ArrayList<>();
		if (postSelection) {
			footer.add(Components.button(
					ButtonStyle.SUCCESS,
					CONTINUE_LISTENER,
					Translatable.text("vocabulary.proceed").resolve(fluctlight)
			));
			footer.add(Components.button(
					ButtonStyle.SECONDARY,
					CONFIRM_LISTENER,
					Translatable.text("vocabulary.confirm").resolve(fluctlight)
			));
		} else {
			footer.add(Components.button(
					ButtonStyle.SECONDARY,
					CANCEL_LISTENER,
					Translatable.text("vocabulary.cancel").resolve(fluctlight)
			));
		}

		List<ActionRow> rows = toActionRows(languageButtons, footer);

		replyOrEdit(event, embed, rows);
	}

	private void showAdditionalLanguageSelection(IReplyCallback event, Fluctlight fluctlight) {
		EmbedBuilder embed = StyleKit.embeds().primary()
				.setTitle(Translatable.text("commands.language.additional.title").resolve(fluctlight))
				.setDescription(Translatable.text("commands.language.additional.description").resolve(fluctlight));

		Set<DiscordLocale> currentAdditionals = fluctlight.getAdditionalLanguages() != null
				? new HashSet<>(Arrays.asList(fluctlight.getAdditionalLanguages()))
				: new HashSet<>();

		// Exclude primary + already-selected additionals
		Set<DiscordLocale> excluded = new HashSet<>(currentAdditionals);
		if (fluctlight.getPrimaryLanguage() != null)
			excluded.add(fluctlight.getPrimaryLanguage());

		List<Button> languageButtons = buildLanguageButtons(
				languagePersistence.getAvailableLanguages(),
				excluded,
				SELECT_ADDITIONAL_LISTENER
		);

		// Footer controls: show Confirm if we have any additionals, otherwise Cancel
		List<Button> footer = new ArrayList<>();
		if (!currentAdditionals.isEmpty()) {
			footer.add(Components.button(
					ButtonStyle.SECONDARY,
					CONFIRM_LISTENER,
					Translatable.text("vocabulary.confirm").resolve(fluctlight)
			));
		} else {
			footer.add(Components.button(
					ButtonStyle.SECONDARY,
					CANCEL_LISTENER,
					Translatable.text("vocabulary.cancel").resolve(fluctlight)
			));
		}

		List<ActionRow> rows = toActionRows(languageButtons, footer);

		replyOrEdit(event, embed, rows);
	}

	private List<Button> buildLanguageButtons(Collection<DiscordLocale> all, Set<DiscordLocale> excluded, String listenerId) {
		if (all == null || all.isEmpty()) return Collections.emptyList();

		Map<DiscordLocale, LanguageEntry> languageConfig = languagesProvider.getObject().toLocaleMap();

		return all.stream()
				.filter(Objects::nonNull)
				.filter(lang -> excluded == null || !excluded.contains(lang))
				.map(lang -> buildLanguageButton(lang, listenerId, languageConfig))
				.map(PayloadButton::getButton)
				.toList();
	}

	private PayloadButton buildLanguageButton(DiscordLocale lang, String listenerId, Map<DiscordLocale, LanguageEntry> languageConfig) {
		LanguageEntry entry = languageConfig.get(lang);
		String emoji = entry != null ? entry.getEmoji() : null;
		String displayName = entry != null ? entry.getDisplayName() : null;

		if (emoji != null && !emoji.isBlank()) {
			try {
				return Components.button(
						ButtonStyle.SECONDARY,
						listenerId,
						Emoji.fromFormatted(emoji),
						lang.getLocale()
				);
			} catch (IllegalArgumentException ignored) {
				// Fall back to label when emoji is invalid.
			}
		}

		String label = (displayName != null && !displayName.isBlank()) ? displayName : fallbackLabel(lang);
		return Components.button(
				ButtonStyle.SECONDARY,
				listenerId,
				label,
				lang.getLocale()
		);
	}

	private String fallbackLabel(DiscordLocale locale) {
		String nativeName = locale.getNativeName();
		if (!nativeName.isBlank())
			return nativeName;

		return locale.getLocale();
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
