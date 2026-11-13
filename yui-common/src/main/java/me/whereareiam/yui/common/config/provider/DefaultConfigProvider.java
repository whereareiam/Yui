package me.whereareiam.yui.common.config.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import jakarta.annotation.PostConstruct;
import me.whereareiam.configura.Configura;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yui.Registry;
import me.whereareiam.yui.config.ConfigProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Base of Yui's own configuration providers: registers each for reloads and reads its file through Yui's
 * Configura instance, never writing it.
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
	 * Reads a file below the base path and fills in what it lacks from the defaults. The file is never written:
	 * it may be read-only, and a missing file means every setting takes its default.
	 *
	 * @param name     the file's name without extension, such as {@code settings}
	 * @param defaults the values a missing setting takes
	 * @return the file's content, completed by the defaults
	 */
	protected T read(String name, T defaults) {
		return configura.merge(getBasePath().resolve(name).toString(), defaults);
	}

	/**
	 * Reads a file below the base path as a tree, to look at it before choosing how to bind it.
	 *
	 * @param name the file's name without extension
	 * @return the file's content, or a missing node when there is no such file
	 */
	protected JsonNode readNode(String name) {
		Path file = getBasePath().resolve(name + configura.extension());
		if (!Files.exists(file)) return MissingNode.getInstance();

		return configura.readNode(file);
	}
}
