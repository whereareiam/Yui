package me.whereareiam.yui.common.plugin.part;

import me.whereareiam.chassis.ExtensionContext;
import org.springframework.context.ApplicationContext;

/**
 * One kind of thing a starting plugin may hold, which Yui registers and later removes.
 */
public interface PluginPart {
	/**
	 * @return undoes what was registered
	 */
	AutoCloseable attach(ApplicationContext plugin, ExtensionContext extension);
}
