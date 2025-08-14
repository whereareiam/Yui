package me.whereareiam.yui.adapter.command.executor.plugin;

import lombok.Getter;
import me.whereareiam.yui.api.model.plugin.InternalPlugin;
import me.whereareiam.yui.api.output.plugin.PluginManager;
import me.whereareiam.yui.api.util.Translatable;

import java.util.*;
import java.util.function.Function;

public final class Category {
	public static final Category ENABLE = new Category(
			"enable",
			"commands.plugin.enable.title",
			"commands.plugin.enable.description",
			"commands.plugin.enable.empty",
			"commands.plugin.enable.format",
			true,
			Category::enableCandidates
	);

	public static final Category DISABLE = new Category(
			"disable",
			"commands.plugin.disable.title",
			"commands.plugin.disable.description",
			"commands.plugin.disable.empty",
			"commands.plugin.disable.format",
			true,
			Category::disableCandidates
	);

	public static final Category UNLOAD = new Category(
			"unload",
			"commands.plugin.unload.title",
			"commands.plugin.unload.description",
			"commands.plugin.unload.empty",
			"commands.plugin.unload.format",
			true,
			Category::unloadCandidates
	);
	
	public static final Category LOAD = new Category(
			"load",
			"commands.plugin.load.title",
			"commands.plugin.load.description",
			null,
			null,
			false,
			_ -> List.of()
	);

	private static final Map<String, Category> BY_ACTION = Map.of(
			ENABLE.action, ENABLE,
			DISABLE.action, DISABLE,
			UNLOAD.action, UNLOAD,
			LOAD.action, LOAD
	);

	@Getter
	private final String action;
	@Getter
	private final String titleKey;
	private final String descriptionKey;
	private final String emptyKey;
	private final String pluginFormatKey;
	private final boolean listsPlugins;
	private final Function<PluginManager, List<InternalPlugin>> candidatesProvider;

	private Category(
			String action,
			String titleKey,
			String descriptionKey,
			String emptyKey,
			String pluginFormatKey,
			boolean listsPlugins,
			Function<PluginManager, List<InternalPlugin>> candidatesProvider
	) {
		this.action = action;
		this.titleKey = titleKey;
		this.descriptionKey = descriptionKey;
		this.emptyKey = emptyKey;
		this.pluginFormatKey = pluginFormatKey;
		this.listsPlugins = listsPlugins;
		this.candidatesProvider = candidatesProvider;
	}

	public static boolean isSupported(String action) {
		return action != null && BY_ACTION.containsKey(action);
	}

	public static Category of(String action) {
		return BY_ACTION.get(action);
	}

	public boolean listsPlugins() {
		return listsPlugins;
	}

	public List<InternalPlugin> candidates(PluginManager pluginManager) {
		return candidatesProvider.apply(pluginManager);
	}

	public String content(PluginManager pluginManager, long userId) {
		if (!listsPlugins) return Translatable.of(descriptionKey, userId);

		List<InternalPlugin> plugins = candidates(pluginManager);
		if (plugins.isEmpty()) return emptyKey == null ? "" : Translatable.of(emptyKey, userId);

		String list = formatPlugins(plugins, userId);
		String description = Translatable.of(descriptionKey, userId);
		return description.replace("{list}", list);
	}

	private String formatPlugins(List<InternalPlugin> plugins, long userId) {
		String format = pluginFormatKey == null ? "{index}. {name} v{version} (``{id}``)" : Translatable.of(pluginFormatKey, userId);
		List<String> lines = new ArrayList<>();
		for (int i = 0; i < plugins.size(); i++) {
			InternalPlugin p = plugins.get(i);
			String authors = p.getPlugin().getAuthors() == null || p.getPlugin().getAuthors().isEmpty()
					? ""
					: String.join(", ", p.getPlugin().getAuthors());

			String line = format
					.replace("{index}", String.valueOf(i + 1))
					.replace("{name}", safe(p.getPlugin().getName()))
					.replace("{version}", safe(p.getPlugin().getVersion()))
					.replace("{authors}", authors)
					.replace("{id}", safe(p.getPlugin().getId()));

			if (authors.isBlank()) line = line.replace(" — ", " ");
			lines.add(line.trim());
		}
		return String.join("\n", lines);
	}

	private static List<InternalPlugin> enableCandidates(PluginManager pluginManager) {
		return sortedPlugins(pluginManager).stream()
				.filter(p -> !p.isEnabled())
				.toList();
	}

	private static List<InternalPlugin> disableCandidates(PluginManager pluginManager) {
		return sortedPlugins(pluginManager).stream()
				.filter(InternalPlugin::isEnabled)
				.toList();
	}

	private static List<InternalPlugin> unloadCandidates(PluginManager pluginManager) {
		return sortedPlugins(pluginManager);
	}

	private static List<InternalPlugin> sortedPlugins(PluginManager pluginManager) {
		return pluginManager.plugins().stream()
				.sorted(Comparator.comparing(p -> p.getPlugin().getName().toLowerCase(Locale.ROOT)))
				.toList();
	}

	private static String safe(String v) {
		return v == null ? "" : v;
	}
}