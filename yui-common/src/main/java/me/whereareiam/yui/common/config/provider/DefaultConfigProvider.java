package me.whereareiam.yui.common.config.provider;

import me.whereareiam.yui.Provider;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yui.config.ConfigProvider;
import me.whereareiam.yui.registry.Registry;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;

/**
 * Adapter-layer base that wires {@link ConfigProvider} into our reload registry
 * and exposes the resolved base path for subclasses.
 */
public abstract class DefaultConfigProvider<T> extends ConfigProvider<T> implements Provider<T> {
	private final Path basePath;

	@Autowired
	protected DefaultConfigProvider(Path basePath, Registry<Reloadable> reloadables) {
		this.basePath = basePath;
		reloadables.register(this);
	}

	protected Path getBasePath() {
		return basePath;
	}
}

