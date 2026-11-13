package me.whereareiam.yui.common.config.provider;

import jakarta.annotation.PostConstruct;
import me.whereareiam.configura.Configura;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yui.Registry;
import me.whereareiam.yui.config.ConfigProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.nio.file.Path;

/**
 * Base of Yui's own configuration providers: registers each for reloads and reads its file through Yui's
 * Configura instance.
 *
 * <p>Files are under the {@code dataPath} base unless a provider names another (styles use their own folder).</p>
 */
public abstract class DefaultConfigProvider<T> extends ConfigProvider<T> {
	@Autowired
	@Qualifier("dataPath")
	private Path basePath;

	@Autowired
	private Registry<Reloadable> reloadables;

	@Autowired
	private Configura configura;

	@PostConstruct
	private void register() {
		reloadables.register(this);
	}

	protected Path getBasePath() {
		return basePath;
	}

	/**
	 * Reads a file below the base path, fills in what it lacks from the defaults, and writes the result back.
	 *
	 * @param name     the file's name without extension, such as {@code settings}
	 * @param defaults the values a missing setting takes
	 * @return the file's content, completed by the defaults
	 */
	protected T update(String name, T defaults) {
		String file = getBasePath().resolve(name).toString();
		T merged = configura.merge(file, defaults);
		configura.write(file, merged);

		return merged;
	}
}
