package me.whereareiam.yui.api.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import me.whereareiam.chassis.ExtensionContext;
import me.whereareiam.yui.api.Registry;
import me.whereareiam.yui.api.Reloadable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base of a plugin's configuration providers: reads a file of the plugin's folder ({@code plugins/<id>/}) and reads it
 * again on {@code /reload} while the plugin runs.
 *
 * <pre>{@code
 * @Component
 * public class SettingsProvider extends PluginConfigProvider<VerificationSettings> {
 *     protected VerificationSettings load() {
 *         return read("settings", new VerificationSettings());
 *     }
 *
 *     public Class<VerificationSettings> getObjectType() {
 *         return VerificationSettings.class;
 *     }
 * }
 * }</pre>
 *
 * @param <T> the model the file is written in
 */
public abstract class PluginConfigProvider<T> extends ConfigProvider<T> {
	@Autowired
	private ExtensionContext extension;

	@Autowired
	private ConfigReader reader;

	@Autowired
	private Registry<Reloadable> reloadables;

	@PostConstruct
	private void register() {
		reloadables.register(this);
	}

	@PreDestroy
	private void unregister() {
		reloadables.unregister(this);
	}

	/**
	 * @param name     the file's name inside the plugin's folder, without extension, such as {@code settings}
	 * @param defaults the values a missing setting takes
	 * @return the file's content, completed by the defaults
	 */
	protected T read(String name, T defaults) {
		return reader.read(extension.dataFolder().resolve(name), defaults);
	}

	/**
	 * @return the reader, to read a default written in code ({@link ConfigReader#parse})
	 */
	protected ConfigReader reader() {
		return reader;
	}
}
