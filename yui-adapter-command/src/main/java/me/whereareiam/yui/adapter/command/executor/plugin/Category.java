package me.whereareiam.yui.adapter.command.executor.plugin;

import lombok.Getter;
import me.whereareiam.chassis.model.ExtensionDescriptor;
import me.whereareiam.chassis.model.InstalledExtension;
import me.whereareiam.chassis.type.ExtensionState;
import me.whereareiam.yui.message.Translatable;
import me.whereareiam.yui.plugin.PluginManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A view of {@code /plugin}: the plugins one action applies to, by their state. Enable lists those that do not run,
 * disable and reload those that do, rescan those still waiting or failed after it.
 */
public final class Category {
	public static final Category ENABLE = new Category("enable", plugin -> plugin.getState() != ExtensionState.ENABLED, true);
	public static final Category DISABLE = new Category("disable", plugin -> plugin.getState() == ExtensionState.ENABLED, true);
	public static final Category RELOAD = new Category("reload", plugin -> plugin.getState() == ExtensionState.ENABLED, true);
	public static final Category RESCAN = new Category("rescan", Category::waitingOrFailed, false);

	private static final Map<String, Category> BY_ACTION = Map.of(
			ENABLE.action, ENABLE,
			DISABLE.action, DISABLE,
			RELOAD.action, RELOAD,
			RESCAN.action, RESCAN
	);

	@Getter
	private final String action;
	private final Predicate<InstalledExtension> applies;
	private final boolean selectable;

	private Category(String action, Predicate<InstalledExtension> applies, boolean selectable) {
		this.action = action;
		this.applies = applies;
		this.selectable = selectable;
	}

	public static boolean isSupported(String action) {
		return action != null && BY_ACTION.containsKey(action);
	}

	public static Category of(String action) {
		return BY_ACTION.get(action);
	}

	public String getTitleKey() {
		return "commands.plugin." + action + ".title";
	}

	/**
	 * @return whether each listed plugin gets a button that applies the action to it
	 */
	public boolean listsPlugins() {
		return selectable;
	}

	public List<InstalledExtension> candidates(PluginManager plugins) {
		return plugins.plugins().stream()
				.filter(applies)
				.sorted(Comparator.comparing(plugin -> name(plugin).toLowerCase(Locale.ROOT)))
				.toList();
	}

	public String content(PluginManager plugins, long user) {
		List<InstalledExtension> listed = candidates(plugins);
		if (listed.isEmpty()) return Translatable.text("commands.plugin." + action + ".empty").resolve(user);

		String format = Translatable.text("commands.plugin." + action + ".format").resolve(user);
		List<String> lines = new ArrayList<>();
		for (int i = 0; i < listed.size(); i++) lines.add(line(format, i + 1, listed.get(i)));

		return Translatable.text("commands.plugin." + action + ".description").resolve(user).replace("<list>", String.join("\n", lines));
	}

	static String line(String format, int index, InstalledExtension plugin) {
		ExtensionDescriptor descriptor = plugin.getDescriptor();
		String authors = descriptor == null ? "" : String.join(", ", descriptor.getAuthors());
		String line = format
				.replace("<index>", String.valueOf(index))
				.replace("<name>", name(plugin))
				.replace("<version>", descriptor == null ? "?" : descriptor.getVersion().toString())
				.replace("<authors>", authors)
				.replace("<id>", plugin.getId())
				.replace("<reason>", reason(plugin));

		return line.trim();
	}

	static String name(InstalledExtension plugin) {
		return plugin.getDescriptor() == null ? plugin.getId() : plugin.getDescriptor().getName();
	}

	private static String reason(InstalledExtension plugin) {
		if (plugin.getFailure() != null) return String.valueOf(plugin.getFailure().getMessage());

		return String.join(", ", plugin.getWaitingFor());
	}

	private static boolean waitingOrFailed(InstalledExtension plugin) {
		return plugin.getState() == ExtensionState.WAITING || plugin.getState() == ExtensionState.FAILED;
	}
}
