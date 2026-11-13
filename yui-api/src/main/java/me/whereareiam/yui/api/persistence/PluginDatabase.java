package me.whereareiam.yui.api.persistence;

import jakarta.persistence.EntityManagerFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Opens the tables a plugin declares with {@link Tables}, in Yui's database. {@link PluginTables} calls it while the
 * plugin's context starts; the plugin itself never does.
 */
public interface PluginDatabase {
	/**
	 * Migrates the plugin's tables and maps its entities.
	 *
	 * @param plugin     the plugin's id
	 * @param loader     the plugin's class loader, which holds its migrations and entities
	 * @param migrations the folder of its migrations on that class path, ending in {@code /}
	 * @param entities   the package of its entities
	 * @return the plugin's own persistence unit; the plugin's context closes it when it stops
	 * @throws IllegalStateException when the plugin has no migrations or entities, or a table not named after it
	 */
	@NotNull EntityManagerFactory open(
			@NotNull String plugin,
			@NotNull ClassLoader loader,
			@NotNull String migrations,
			@NotNull String entities
	);
}
