package me.whereareiam.yue.api.model.module;

import me.whereareiam.yue.api.output.module.YueModule;
import me.whereareiam.yue.api.type.ModuleState;

import java.nio.file.Path;
import java.util.List;

/**
 * Represents an internal module configuration with additional runtime properties.
 * Extends the base {@link Module} class to add functionality specific to the
 * Yue Discord Bot framework.
 * <p>
 * This class maintains module state, file path, and the associated module instance,
 * providing essential information for module management during runtime.
 */
public class InternalModule extends Module {
	/**
	 * The file system path to the module
	 */
	private final Path path;

	/**
	 * The loaded module instance
	 */
	private YueModule module;

	/**
	 * The current state of the module
	 */
	private ModuleState state;

	/**
	 * Constructs a new InternalModule instance with the specified parameters.
	 *
	 * @param path       The file system path to the module
	 * @param yueModule  The loaded module instance
	 * @param state      The current state of the module
	 * @param moduleInfo The base module information
	 */
	public InternalModule(Path path, YueModule yueModule, ModuleState state, Module moduleInfo) {
		super(moduleInfo.getName(), moduleInfo.getVersion(), moduleInfo.getAuthors(),
				moduleInfo.getSupportedVersions(), moduleInfo.getMain());

		this.path = path;
		this.module = yueModule;
		this.state = state;
	}

	/**
	 * Constructs a new InternalModule instance with the specified parameters.
	 *
	 * @param name              The name of the module
	 * @param version           The version string of the module
	 * @param authors           List of module authors
	 * @param supportedVersions List of game versions this module supports
	 * @param main              The main class path of the module
	 * @param path              The file system path to the module
	 * @param yueModule         The loaded module instance
	 * @param state             The current state of the module
	 */
	public InternalModule(String name, String version, List<String> authors, List<String> supportedVersions,
	                      String main, Path path, YueModule yueModule, ModuleState state) {
		super(name, version, authors, supportedVersions, main);

		this.path = path;
		this.module = yueModule;
		this.state = state;
	}

	public Path getPath() {
		return path;
	}

	public void setModule(YueModule module) {
		this.module = module;
	}

	public YueModule getModule() {
		return module;
	}

	public ModuleState getState() {
		return state;
	}

	public void setState(ModuleState state) {
		this.state = state;
	}
}