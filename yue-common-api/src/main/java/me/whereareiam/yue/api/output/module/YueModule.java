package me.whereareiam.yue.api.output.module;

import me.whereareiam.yue.api.model.module.InternalModule;

import java.nio.file.Path;

/**
 * Base class for all Yue modules.
 * Provides the lifecycle methods and core functionality for module management.
 *
 * <p>Module lifecycle:</p>
 * <ol>
 *   <li>{@link #onLoad()} - Called when the module is first loaded</li>
 *   <li>{@link #onEnable()} - Called when the module is enabled</li>
 *   <li>{@link #onDisable()} - Called when the module is disabled</li>
 *   <li>{@link #onUnload()} - Called when the module is being unloaded</li>
 * </ol>
 */
public abstract class YueModule {
	/**
	 * Internal module configuration and metadata
	 */
	protected InternalModule module;

	/**
	 * Working directory path for the module
	 */
	protected Path workingPath;

	/**
	 * Called when the module is being loaded.
	 * Use this method to initialize resources and load configurations.
	 */
	public abstract void onLoad();

	/**
	 * Called when the module is being enabled.
	 * Use this method to start module functionality.
	 */
	public abstract void onEnable();

	/**
	 * Called when the module is being disabled.
	 * Use this method to stop module functionality.
	 */
	public abstract void onDisable();

	/**
	 * Called when the module is being unloaded.
	 * Use this method to clean up resources and save data.
	 */
	public abstract void onUnload();

	public InternalModule getModule() {
		return module;
	}

	public void setModule(InternalModule module) {
		this.module = module;
	}

	public Path getWorkingPath() {
		return workingPath;
	}

	public void setWorkingPath(Path workingPath) {
		this.workingPath = workingPath;
	}
}