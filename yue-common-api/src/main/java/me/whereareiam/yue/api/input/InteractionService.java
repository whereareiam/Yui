package me.whereareiam.yue.api.input;

import me.whereareiam.yue.api.model.PayloadButton;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.List;
import java.util.function.Consumer;

public interface InteractionService {
	Button createButton(ButtonStyle style, String path, String label);

	Button createButton(ButtonStyle style, String path, Emoji emoji);

	PayloadButton createButton(ButtonStyle style, String path, String label, String payload);

	PayloadButton createButton(ButtonStyle style, String path, Emoji emoji, String payload);

	StringSelectMenu createStringSelectMenu(String path, List<SelectOption> options);

	EntitySelectMenu createEntitySelectMenu(String path, EntitySelectMenu.SelectTarget target, int min, int max);

	String getPayload(GenericComponentInteractionCreateEvent event);

	<E extends GenericComponentInteractionCreateEvent> void registerHandler(String path, Class<E> type, Consumer<E> handler);
}