package me.whereareiam.yui.util;

import me.whereareiam.yui.model.Key;
import me.whereareiam.yui.model.PayloadButton;
import me.whereareiam.yui.model.component.ComponentAttributes;
import me.whereareiam.yui.service.InteractionService;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Static helpers for creating components and binding interaction handlers.
 */
@Component
@SuppressWarnings("unused")
public class Components {
	private static InteractionService interactions;

	/**
	 * Initializes static dependencies for component helpers.
	 *
	 * @param interactionService interaction service
	 */
	@Autowired
	public void init(@NotNull InteractionService interactionService) {
		Components.interactions = interactionService;
	}

	/**
	 * Creates a button with the given style and label.
	 *
	 * @param style button style
	 * @param path interaction path
	 * @param label button label
	 * @return created button
	 */
	public static @NotNull Button button(@NotNull ButtonStyle style, @NotNull String path, @NotNull String label) {
		return interactions.createButton(style, path, label);
	}

	/**
	 * Creates a button with the given style and emoji.
	 *
	 * @param style button style
	 * @param path interaction path
	 * @param emoji button emoji
	 * @return created button
	 */
	public static @NotNull Button button(@NotNull ButtonStyle style, @NotNull String path, @NotNull Emoji emoji) {
		return interactions.createButton(style, path, emoji);
	}

	/**
	 * Creates a button with the given style, label and payload.
	 *
	 * @param style button style
	 * @param path interaction path
	 * @param label button label
	 * @param payload stored payload
	 * @return payload button wrapper
	 */
	public static @NotNull PayloadButton button(
			@NotNull ButtonStyle style,
			@NotNull String path,
			@NotNull String label,
			@NotNull String payload
	) {
		return interactions.createButton(style, path, label, payload);
	}

	/**
	 * Creates a button with the given style, emoji and payload.
	 *
	 * @param style button style
	 * @param path interaction path
	 * @param emoji button emoji
	 * @param payload stored payload
	 * @return payload button wrapper
	 */
	public static @NotNull PayloadButton button(
			@NotNull ButtonStyle style,
			@NotNull String path,
			@NotNull Emoji emoji,
			@NotNull String payload
	) {
		return interactions.createButton(style, path, emoji, payload);
	}

	/**
	 * Creates a button with the given style, label and attributes.
	 *
	 * @param style button style
	 * @param path interaction path
	 * @param label button label
	 * @param attributes component attributes
	 * @return created button
	 */
	public static @NotNull Button button(
			@NotNull ButtonStyle style,
			@NotNull String path,
			@NotNull String label,
			@NotNull ComponentAttributes attributes
	) {
		return interactions.createButton(style, path, label, attributes);
	}

	/**
	 * Creates a button with the given style, emoji and attributes.
	 *
	 * @param style button style
	 * @param path interaction path
	 * @param emoji button emoji
	 * @param attributes component attributes
	 * @return created button
	 */
	public static @NotNull Button button(
			@NotNull ButtonStyle style,
			@NotNull String path,
			@NotNull Emoji emoji,
			@NotNull ComponentAttributes attributes
	) {
		return interactions.createButton(style, path, emoji, attributes);
	}

	/**
	 * Creates a button with the given style, label, payload and attributes.
	 *
	 * @param style button style
	 * @param path interaction path
	 * @param label button label
	 * @param payload stored payload
	 * @param attributes component attributes
	 * @return payload button wrapper
	 */
	public static @NotNull PayloadButton button(
			@NotNull ButtonStyle style,
			@NotNull String path,
			@NotNull String label,
			@NotNull String payload,
			@NotNull ComponentAttributes attributes
	) {
		return interactions.createButton(style, path, label, payload, attributes);
	}

	/**
	 * Creates a button with the given style, emoji, payload and attributes.
	 *
	 * @param style button style
	 * @param path interaction path
	 * @param emoji button emoji
	 * @param payload stored payload
	 * @param attributes component attributes
	 * @return payload button wrapper
	 */
	public static @NotNull PayloadButton button(
			@NotNull ButtonStyle style,
			@NotNull String path,
			@NotNull Emoji emoji,
			@NotNull String payload,
			@NotNull ComponentAttributes attributes
	) {
		return interactions.createButton(style, path, emoji, payload, attributes);
	}

	/**
	 * Creates a string select menu builder.
	 *
	 * @param path interaction path
	 * @return menu builder
	 */
	public static @NotNull StringSelectMenu.Builder menu(@NotNull String path) {
		return interactions.createStringSelectMenu(path);
	}

	/**
	 * Creates a string select menu builder with attributes.
	 *
	 * @param path interaction path
	 * @param attributes component attributes
	 * @return menu builder
	 */
	public static @NotNull StringSelectMenu.Builder menu(
			@NotNull String path,
			@NotNull ComponentAttributes attributes
	) {
		return interactions.createStringSelectMenu(path, attributes);
	}

	/**
	 * Creates an entity select menu builder.
	 *
	 * @param path interaction path
	 * @param t select target
	 * @return menu builder
	 */
	public static @NotNull EntitySelectMenu.Builder menu(
			@NotNull String path,
			@NotNull EntitySelectMenu.SelectTarget t
	) {
		return interactions.createEntitySelectMenu(path, t);
	}

	/**
	 * Creates an entity select menu builder with attributes.
	 *
	 * @param path interaction path
	 * @param t select target
	 * @param attributes component attributes
	 * @return menu builder
	 */
	public static @NotNull EntitySelectMenu.Builder menu(
			@NotNull String path,
			@NotNull EntitySelectMenu.SelectTarget t,
			@NotNull ComponentAttributes attributes
	) {
		return interactions.createEntitySelectMenu(path, t, attributes);
	}

	/**
	 * Returns the payload stored for this interaction.
	 *
	 * @param event interaction event
	 * @return payload or null when not found
	 */
	public static @Nullable String payload(@NotNull GenericComponentInteractionCreateEvent event) {
		return interactions.getPayload(event);
	}

	/**
	 * Returns component attributes bound to this interaction.
	 *
	 * @param event interaction event
	 * @return component attributes
	 */
	public static @NotNull ComponentAttributes attributes(@NotNull GenericComponentInteractionCreateEvent event) {
		return interactions.getAttributes(event);
	}

	/**
	 * Returns a typed attribute value for this interaction.
	 *
	 * @param event interaction event
	 * @param key attribute key
	 * @param <T> attribute value type
	 * @return optional attribute value
	 */
	public static <T> @NotNull Optional<T> attribute(
			@NotNull GenericComponentInteractionCreateEvent event,
			@NotNull Key<T> key
	) {
		return interactions.getAttributes(event).get(key);
	}

	/**
	 * Registers a handler for the given component path.
	 *
	 * @param path interaction path
	 * @param type event type
	 * @param handler event handler
	 * @param <E> event type
	 */
	public static <E extends GenericComponentInteractionCreateEvent> void on(
			@NotNull String path,
			@NotNull Class<E> type,
			@NotNull Consumer<E> handler
	) {
		interactions.registerHandler(path, type, handler);
	}
}
