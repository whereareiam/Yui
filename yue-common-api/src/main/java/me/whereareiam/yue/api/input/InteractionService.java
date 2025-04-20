package me.whereareiam.yue.api.input;

import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.List;
import java.util.function.Consumer;

public interface InteractionService {
	Button createButton(String path, String label);

	StringSelectMenu createStringSelectMenu(String path, List<SelectOption> options);

	EntitySelectMenu createEntitySelectMenu(String path, EntitySelectMenu.SelectTarget target, int min, int max);

	<E extends GenericComponentInteractionCreateEvent> void registerHandler(String path, Class<E> type, Consumer<E> handler);
}