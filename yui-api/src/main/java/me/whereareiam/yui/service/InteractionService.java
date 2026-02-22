package me.whereareiam.yui.service;

import me.whereareiam.yui.model.Key;
import me.whereareiam.yui.model.PayloadButton;
import me.whereareiam.yui.model.component.ComponentAttributes;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Service for creating component interactions and binding handlers.
 */
public interface InteractionService {
	/**
	 * Creates a button with the given style and label.
	 *
	 * @param style button style
	 * @param path interaction path
	 * @param label button label
	 * @return created button
	 */
	@NotNull Button createButton(@NotNull ButtonStyle style, @NotNull String path, @NotNull String label);

	/**
	 * Creates a button with the given style and emoji.
	 *
	 * @param style button style
	 * @param path interaction path
	 * @param emoji button emoji
	 * @return created button
	 */
	@NotNull Button createButton(@NotNull ButtonStyle style, @NotNull String path, @NotNull Emoji emoji);

	/**
	 * Creates a button with the given style, label and payload.
	 *
	 * @param style button style
	 * @param path interaction path
	 * @param label button label
	 * @param payload stored payload
	 * @return payload button wrapper
	 */
	@NotNull PayloadButton createButton(
			@NotNull ButtonStyle style,
			@NotNull String path,
			@NotNull String label,
			@NotNull String payload
	);

	/**
	 * Creates a button with the given style, emoji and payload.
	 *
	 * @param style button style
	 * @param path interaction path
	 * @param emoji button emoji
	 * @param payload stored payload
	 * @return payload button wrapper
	 */
	@NotNull PayloadButton createButton(
			@NotNull ButtonStyle style,
			@NotNull String path,
			@NotNull Emoji emoji,
			@NotNull String payload
	);

	/**
	 * Creates a button with the given style, label and attributes.
	 *
	 * @param style button style
	 * @param path interaction path
	 * @param label button label
	 * @param attributes component attributes
	 * @return created button
	 */
	@NotNull Button createButton(
			@NotNull ButtonStyle style,
			@NotNull String path,
			@NotNull String label,
			@NotNull ComponentAttributes attributes
	);

	/**
	 * Creates a button with the given style, emoji and attributes.
	 *
	 * @param style button style
	 * @param path interaction path
	 * @param emoji button emoji
	 * @param attributes component attributes
	 * @return created button
	 */
	@NotNull Button createButton(
			@NotNull ButtonStyle style,
			@NotNull String path,
			@NotNull Emoji emoji,
			@NotNull ComponentAttributes attributes
	);

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
	@NotNull PayloadButton createButton(
			@NotNull ButtonStyle style,
			@NotNull String path,
			@NotNull String label,
			@NotNull String payload,
			@NotNull ComponentAttributes attributes
	);

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
	@NotNull PayloadButton createButton(
			@NotNull ButtonStyle style,
			@NotNull String path,
			@NotNull Emoji emoji,
			@NotNull String payload,
			@NotNull ComponentAttributes attributes
	);

	/**
	 * Creates a string select menu builder.
	 *
	 * @param path interaction path
	 * @return menu builder
	 */
	@NotNull StringSelectMenu.Builder createStringSelectMenu(@NotNull String path);

	/**
	 * Creates a string select menu builder with attributes.
	 *
	 * @param path interaction path
	 * @param attributes component attributes
	 * @return menu builder
	 */
	@NotNull StringSelectMenu.Builder createStringSelectMenu(
			@NotNull String path,
			@NotNull ComponentAttributes attributes
	);

	/**
	 * Creates an entity select menu builder.
	 *
	 * @param path interaction path
	 * @param t select target
	 * @return menu builder
	 */
	@NotNull EntitySelectMenu.Builder createEntitySelectMenu(
			@NotNull String path,
			@NotNull EntitySelectMenu.SelectTarget t
	);

	/**
	 * Creates an entity select menu builder with attributes.
	 *
	 * @param path interaction path
	 * @param t select target
	 * @param attributes component attributes
	 * @return menu builder
	 */
	@NotNull EntitySelectMenu.Builder createEntitySelectMenu(
			@NotNull String path,
			@NotNull EntitySelectMenu.SelectTarget t,
			@NotNull ComponentAttributes attributes
	);

	/**
	 * Returns the payload stored for this interaction.
	 *
	 * @param event interaction event
	 * @return payload or null when not found
	 */
	@Nullable String getPayload(@NotNull GenericComponentInteractionCreateEvent event);

	/**
	 * Returns component attributes bound to this interaction.
	 *
	 * @param event interaction event
	 * @return component attributes
	 */
	@NotNull ComponentAttributes getAttributes(@NotNull GenericComponentInteractionCreateEvent event);

	/**
	 * Binds attributes to the given component id.
	 *
	 * @param componentId component id
	 * @param attributes component attributes
	 */
	void bindAttributes(@NotNull String componentId, @NotNull ComponentAttributes attributes);

	/**
	 * Removes attributes for the given component id.
	 *
	 * @param componentId component id
	 */
	void unbindAttributes(@NotNull String componentId);

	/**
	 * Removes any bindings that contain the given attribute value.
	 *
	 * @param key attribute key
	 * @param value attribute value
	 * @param <T> attribute value type
	 */
	<T> void unbindAllByAttribute(@NotNull Key<T> key, @NotNull T value);

	/**
	 * Registers a handler for the given component path.
	 *
	 * @param path interaction path
	 * @param type event type
	 * @param handler event handler
	 * @param <E> event type
	 */
	<E extends GenericComponentInteractionCreateEvent> void registerHandler(
			@NotNull String path,
			@NotNull Class<E> type,
			@NotNull Consumer<E> handler
	);

	/**
	 * Unregisters all handlers and payloads belonging to the given plugin.
	 *
	 * @param pluginId plugin id
	 */
	void unregister(@Nullable String pluginId);
}
