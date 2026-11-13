package me.whereareiam.yui.api.plugin;

import me.whereareiam.chassis.Extension;
import me.whereareiam.chassis.ExtensionContext;

/**
 * A plugin of Yui. Its main class implements this and carries {@link Plugin}, which makes it the Spring configuration
 * of the plugin's own context; Yui registers the plugin's listeners, commands, texts and condition types itself, so
 * {@code enable} and {@code disable} only hold what the plugin does beyond them.
 *
 * <pre>{@code
 * @Plugin
 * public class Verification implements YuiPlugin {
 * }
 * }</pre>
 */
public interface YuiPlugin extends Extension<ExtensionContext> {
	@Override
	default void enable(ExtensionContext context) {
	}

	@Override
	default void disable() {
	}
}
