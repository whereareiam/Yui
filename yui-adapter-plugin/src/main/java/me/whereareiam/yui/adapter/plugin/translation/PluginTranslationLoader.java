package me.whereareiam.yui.adapter.plugin.translation;

import me.whereareiam.yui.Constants;
import me.whereareiam.yui.message.MessageDefaults;
import me.whereareiam.yui.message.MessageLayers;
import me.whereareiam.yui.model.plugin.InternalPlugin;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Gives each plugin its texts as layers it owns: its {@link MessageDefaults}, then its own
 * {@code plugins/<name>/messages/<locale>/} over them. Every key starts with the plugin's id
 * ({@code verification.commands.*}), so a plugin's files cannot meet Yui's or another plugin's. The layers go when
 * the plugin unloads.
 */
@Component
public class PluginTranslationLoader {
	private final MessageLayers layers;
	private final Path pluginsPath;

	public PluginTranslationLoader(MessageLayers layers, @Qualifier("pluginsPath") Path pluginsPath) {
		this.layers = layers;
		this.pluginsPath = pluginsPath;
	}

	public void loadPlugin(InternalPlugin plugin) {
		String id = id(plugin);
		List<MessageDefaults<?>> defaults = new ArrayList<>();
		for (MessageDefaults<?> file : plugin.getContext().getBeansOfType(MessageDefaults.class).values()) defaults.add(file);

		Path folder = pluginsPath.resolve(plugin.getPlugin().getName().replaceAll("[^a-zA-Z0-9.-]", "_"))
				.resolve(Constants.Structure.messagesDir);

		layers.add(id, defaults, folder);
	}

	public void unloadPlugin(InternalPlugin plugin) {
		layers.remove(id(plugin));
	}

	private static String id(InternalPlugin plugin) {
		return plugin.getPlugin().getId().toLowerCase(Locale.ROOT);
	}
}
