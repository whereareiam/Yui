package me.whereareiam.yui.util;

import me.whereareiam.yui.service.InteractionService;
import me.whereareiam.yui.model.PayloadButton;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@SuppressWarnings("unused")
public class Components {
	private static InteractionService interactions;

	@Autowired
	public void init(InteractionService interactionService) {
		Components.interactions = interactionService;
	}

	public static Button button(ButtonStyle style, String path, String label) {
		return interactions.createButton(style, path, label);
	}

	public static Button button(ButtonStyle style, String path, Emoji emoji) {
		return interactions.createButton(style, path, emoji);
	}

	public static PayloadButton button(ButtonStyle style, String path, String label, String payload) {
		return interactions.createButton(style, path, label, payload);
	}

	public static PayloadButton button(ButtonStyle style, String path, Emoji emoji, String payload) {
		return interactions.createButton(style, path, emoji, payload);
	}

	public static StringSelectMenu.Builder menu(String path) {
		return interactions.createStringSelectMenu(path);
	}

	public static EntitySelectMenu.Builder menu(String path, EntitySelectMenu.SelectTarget t) {
		return interactions.createEntitySelectMenu(path, t);
	}

	public static String payload(GenericComponentInteractionCreateEvent event) {
		return interactions.getPayload(event);
	}

	public static <E extends GenericComponentInteractionCreateEvent> void on(String path, Class<E> type, Consumer<E> handler) {
		interactions.registerHandler(path, type, handler);
	}
}
