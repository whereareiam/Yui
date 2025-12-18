package me.whereareiam.yui.common.config.provider;

import jakarta.annotation.PostConstruct;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yui.common.config.ConfiguraBootstrap;
import me.whereareiam.yui.config.ConfigProvider;
import me.whereareiam.yui.registry.Registry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.nio.file.Path;

/**
 * Adapter-layer base that wires {@link ConfigProvider} into our reload registry
 * and exposes the resolved base path for subclasses.
 * <p>
 * By default uses the {@code dataPath} base. Configs that need another base
 * (e.g. styles or languages) can override {@link #getBasePath()}.
 * <p>
 * Also depends on {@link ConfiguraBootstrap} to ensure Configura is initialized
 * (reader/writer, adapters) before any configuration is loaded.
 */
public abstract class DefaultConfigProvider<T> extends ConfigProvider<T> {
	@Autowired
	@Qualifier("dataPath")
	private Path basePath;

	@Autowired
	private Registry<Reloadable> reloadables;

	/**
	 * Only injected to enforce lifecycle ordering (ConfiguraBootstrap runs first).
	 */
	@Autowired
	@SuppressWarnings("unused")
	private ConfiguraBootstrap configuraBootstrap;

	@PostConstruct
	private void register() {
		reloadables.register(this);
	}

	protected Path getBasePath() {
		return basePath;
	}
}
