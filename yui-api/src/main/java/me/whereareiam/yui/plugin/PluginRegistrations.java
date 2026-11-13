package me.whereareiam.yui.plugin;

import me.whereareiam.chassis.ExtensionContext;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;

/**
 * Registers what a starting plugin holds: its JDA and component listeners, its definition providers and commands, its
 * texts and its condition types and audiences.
 */
public interface PluginRegistrations {
	/**
	 * @param plugin    the plugin's Spring context
	 * @param extension the plugin's context of this load
	 * @return undoes every registration, the last first
	 */
	@NotNull AutoCloseable attach(@NotNull ApplicationContext plugin, @NotNull ExtensionContext extension);
}
