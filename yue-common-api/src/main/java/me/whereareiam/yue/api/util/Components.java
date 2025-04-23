package me.whereareiam.yue.api.util;

import me.whereareiam.yue.api.input.InteractionService;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Component
public class Components {
	private static InteractionService interactions;

	@Autowired
	public void init(InteractionService interactionService) {
		Components.interactions = interactionService;
	}

	public static Button button(String path, String label) {
		return interactions.createButton(path, label);
	}

	public static StringSelectMenu menu(String path, List<SelectOption> options) {
		return interactions.createStringSelectMenu(path, options);
	}

	public static EntitySelectMenu menu(String path, EntitySelectMenu.SelectTarget t, int min, int max) {
		return interactions.createEntitySelectMenu(path, t, min, max);
	}

	public static <E extends GenericComponentInteractionCreateEvent> void on(String path, Class<E> type, Consumer<E> handler) {
		interactions.registerHandler(path, type, handler);
	}
}
