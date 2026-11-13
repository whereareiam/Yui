package me.whereareiam.yui.verification.journey.step;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.journey.JourneyKeys;
import me.whereareiam.yui.api.message.Translatable;
import me.whereareiam.yui.api.model.component.ComponentAttributes;
import me.whereareiam.yui.api.model.config.languages.Language;
import me.whereareiam.yui.api.model.config.languages.Languages;
import me.whereareiam.yui.api.model.fluctlight.Fluctlight;
import me.whereareiam.yui.api.persistence.LanguagePersistence;
import me.whereareiam.yui.api.service.InteractionService;
import me.whereareiam.yui.verification.journey.Choice;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * The buttons of a step's message: one per language the server offers that the member may still pick, with its emoji
 * from {@code languages.yml} or its name, and optionally a button to go on, five to a row.
 */
@Component
@RequiredArgsConstructor
class LanguageButtons {
	private static final int ROW = 5;

	private final InteractionService interactions;
	private final LanguagePersistence offered;
	private final ObjectProvider<Languages> languages;

	/**
	 * @return the languages the server offers that pass the filter
	 */
	List<DiscordLocale> offered(Predicate<DiscordLocale> filter) {
		return offered.getAvailableLanguages().stream().filter(Objects::nonNull).filter(filter).toList();
	}

	List<ActionRow> rows(List<DiscordLocale> choices, Fluctlight reader, boolean proceed, String session) {
		ComponentAttributes attributes = JourneyKeys.forSession(session);
		List<Button> buttons = new ArrayList<>();
		choices.forEach(language -> buttons.add(button(language, reader, attributes)));
		if (proceed) {
			String label = Translatable.text("words.proceed").resolve(reader);
			buttons.add(interactions.createButton(ButtonStyle.SUCCESS, Choice.CONTINUE.getPath(), label, attributes));
		}

		List<ActionRow> rows = new ArrayList<>();
		for (int start = 0; start < buttons.size(); start += ROW)
			rows.add(ActionRow.of(buttons.subList(start, Math.min(start + ROW, buttons.size()))));

		return rows;
	}

	private Button button(DiscordLocale language, Fluctlight reader, ComponentAttributes attributes) {
		String path = Choice.LANGUAGE.getPath();
		String emoji = languages.getObject().get(language).map(Language::getEmoji).orElse(null);
		if (emoji != null && !emoji.isBlank())
			return interactions.createButton(ButtonStyle.SECONDARY, path, Emoji.fromFormatted(emoji), language.getLocale(), attributes).getButton();

		return interactions.createButton(ButtonStyle.SECONDARY, path, name(language, reader), language.getLocale(), attributes).getButton();
	}

	// A language's name is a text of messages/<locale>/languages.yml; without one, Discord's own.
	private static String name(DiscordLocale language, Fluctlight reader) {
		String key = "languages." + language.getLocale();
		String name = Translatable.text(key).resolve(reader);

		return name.equals(key) ? language.getNativeName() : name;
	}
}
