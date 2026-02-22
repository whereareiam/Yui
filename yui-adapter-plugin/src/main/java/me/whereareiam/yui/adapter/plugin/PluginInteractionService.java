package me.whereareiam.yui.adapter.plugin;

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

import java.util.function.Consumer;

/**
 * Plugin-local InteractionService wrapper that qualifies paths with the plugin id.
 */
public class PluginInteractionService implements InteractionService {
	private final @NotNull String pluginId;
	private final @NotNull InteractionService delegate;

	public PluginInteractionService(@NotNull String pluginId, @NotNull InteractionService delegate) {
		this.pluginId = pluginId;
		this.delegate = delegate;
	}

	private @NotNull String qualify(@Nullable String path) {
		if (path == null) return pluginId + ":";
		return path.indexOf(':') >= 0 ? path : pluginId + ":" + path;
	}

	@Override
	public @NotNull Button createButton(@NotNull ButtonStyle style, @NotNull String path, @NotNull String label) {
		return delegate.createButton(style, qualify(path), label);
	}

	@Override
	public @NotNull Button createButton(@NotNull ButtonStyle style, @NotNull String path, @NotNull Emoji emoji) {
		return delegate.createButton(style, qualify(path), emoji);
	}

	@Override
	public @NotNull PayloadButton createButton(
			@NotNull ButtonStyle style,
			@NotNull String path,
			@NotNull String label,
			@NotNull String payload
	) {
		return delegate.createButton(style, qualify(path), label, payload);
	}

	@Override
	public @NotNull PayloadButton createButton(
			@NotNull ButtonStyle style,
			@NotNull String path,
			@NotNull Emoji emoji,
			@NotNull String payload
	) {
		return delegate.createButton(style, qualify(path), emoji, payload);
	}

	@Override
	public @NotNull Button createButton(
			@NotNull ButtonStyle style,
			@NotNull String path,
			@NotNull String label,
			@NotNull ComponentAttributes attributes
	) {
		return delegate.createButton(style, qualify(path), label, attributes);
	}

	@Override
	public @NotNull Button createButton(
			@NotNull ButtonStyle style,
			@NotNull String path,
			@NotNull Emoji emoji,
			@NotNull ComponentAttributes attributes
	) {
		return delegate.createButton(style, qualify(path), emoji, attributes);
	}

	@Override
	public @NotNull PayloadButton createButton(
			@NotNull ButtonStyle style,
			@NotNull String path,
			@NotNull String label,
			@NotNull String payload,
			@NotNull ComponentAttributes attributes
	) {
		return delegate.createButton(style, qualify(path), label, payload, attributes);
	}

	@Override
	public @NotNull PayloadButton createButton(
			@NotNull ButtonStyle style,
			@NotNull String path,
			@NotNull Emoji emoji,
			@NotNull String payload,
			@NotNull ComponentAttributes attributes
	) {
		return delegate.createButton(style, qualify(path), emoji, payload, attributes);
	}

	@Override
	public @NotNull StringSelectMenu.Builder createStringSelectMenu(@NotNull String path) {
		return delegate.createStringSelectMenu(qualify(path));
	}

	@Override
	public @NotNull StringSelectMenu.Builder createStringSelectMenu(
			@NotNull String path,
			@NotNull ComponentAttributes attributes
	) {
		return delegate.createStringSelectMenu(qualify(path), attributes);
	}

	@Override
	public @NotNull EntitySelectMenu.Builder createEntitySelectMenu(
			@NotNull String path,
			@NotNull EntitySelectMenu.SelectTarget t
	) {
		return delegate.createEntitySelectMenu(qualify(path), t);
	}

	@Override
	public @NotNull EntitySelectMenu.Builder createEntitySelectMenu(
			@NotNull String path,
			@NotNull EntitySelectMenu.SelectTarget t,
			@NotNull ComponentAttributes attributes
	) {
		return delegate.createEntitySelectMenu(qualify(path), t, attributes);
	}

	@Override
	public @Nullable String getPayload(@NotNull GenericComponentInteractionCreateEvent event) {
		return delegate.getPayload(event);
	}

	@Override
	public @NotNull ComponentAttributes getAttributes(@NotNull GenericComponentInteractionCreateEvent event) {
		return delegate.getAttributes(event);
	}

	@Override
	public void bindAttributes(@NotNull String componentId, @NotNull ComponentAttributes attributes) {
		delegate.bindAttributes(componentId, attributes);
	}

	@Override
	public void unbindAttributes(@NotNull String componentId) {
		delegate.unbindAttributes(componentId);
	}

	@Override
	public <T> void unbindAllByAttribute(@NotNull Key<T> key, @NotNull T value) {
		delegate.unbindAllByAttribute(key, value);
	}

	@Override
	public <E extends GenericComponentInteractionCreateEvent> void registerHandler(
			@NotNull String path,
			@NotNull Class<E> type,
			@NotNull Consumer<E> handler
	) {
		delegate.registerHandler(qualify(path), type, handler);
	}

	@Override
	public void unregister(@Nullable String pluginId) {
		delegate.unregister(pluginId);
	}
}
