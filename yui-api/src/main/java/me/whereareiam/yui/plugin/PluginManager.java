package me.whereareiam.yui.plugin;

import me.whereareiam.chassis.exception.ExtensionException;
import me.whereareiam.chassis.model.InstalledExtension;
import me.whereareiam.yui.model.update.UpdateConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Yui's plugins, as Chassis keeps them: every jar in {@code plugins/}, enabled, disabled, waiting or failed. Changes
 * are made one at a time; disabling is remembered in {@code plugins.yml}. A change names an installed plugin, or throws
 * {@link ExtensionException}.
 */
public interface PluginManager {
	/**
	 * @return every installed plugin, the running ones in the order they started
	 */
	@NotNull List<InstalledExtension> plugins();

	@NotNull Optional<InstalledExtension> find(@NotNull String id);

	@NotNull InstalledExtension enable(@NotNull String id) throws ExtensionException;

	@NotNull InstalledExtension disable(@NotNull String id) throws ExtensionException;

	/**
	 * Stops the plugin and those that depend on it, reads its jar again and starts them again.
	 */
	@NotNull InstalledExtension reload(@NotNull String id) throws ExtensionException;

	/**
	 * Installs jars added to the folder since and starts what can start now.
	 *
	 * @return every installed plugin
	 */
	@NotNull List<InstalledExtension> rescan();

	/**
	 * @param id a plugin's id
	 * @return where its updates come from, as its descriptor names them
	 */
	@NotNull Optional<UpdateConfiguration> updater(@NotNull String id);
}
