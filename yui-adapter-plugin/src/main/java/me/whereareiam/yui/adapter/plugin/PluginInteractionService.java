package me.whereareiam.yui.adapter.plugin;

import me.whereareiam.yui.service.InteractionService;
import me.whereareiam.yui.model.PayloadButton;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.function.Consumer;

/**
 * Plugin-local InteractionService wrapper that qualifies paths with the plugin id.
 */
public class PluginInteractionService implements InteractionService {
	private final String pluginId;
	private final InteractionService delegate;

	public PluginInteractionService(String pluginId, InteractionService delegate) {
		this.pluginId = pluginId;
		this.delegate = delegate;
	}

	private String qualify(String path) {
		if (path == null) return pluginId + ":";
		return path.indexOf(':') >= 0 ? path : pluginId + ":" + path;
	}

	@Override
	public Button createButton(ButtonStyle style, String path, String label) {
		return delegate.createButton(style, qualify(path), label);
	}

	@Override
	public Button createButton(ButtonStyle style, String path, Emoji emoji) {
		return delegate.createButton(style, qualify(path), emoji);
	}

	@Override
	public PayloadButton createButton(ButtonStyle style, String path, String label, String payload) {
		return delegate.createButton(style, qualify(path), label, payload);
	}

	@Override
	public PayloadButton createButton(ButtonStyle style, String path, Emoji emoji, String payload) {
		return delegate.createButton(style, qualify(path), emoji, payload);
	}

	@Override
	public StringSelectMenu.Builder createStringSelectMenu(String path) {
		return delegate.createStringSelectMenu(qualify(path));
	}

	@Override
	public EntitySelectMenu.Builder createEntitySelectMenu(String path, EntitySelectMenu.SelectTarget t) {
		return delegate.createEntitySelectMenu(qualify(path), t);
	}

	@Override
	public String getPayload(GenericComponentInteractionCreateEvent event) {
		return delegate.getPayload(event);
	}

	@Override
	public <E extends GenericComponentInteractionCreateEvent> void registerHandler(String path, Class<E> type, Consumer<E> handler) {
		delegate.registerHandler(qualify(path), type, handler);
	}

	@Override
	public void unregister(String pluginId) {
		delegate.unregister(pluginId);
	}
}


