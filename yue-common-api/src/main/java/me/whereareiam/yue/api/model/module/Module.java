package me.whereareiam.yue.api.model.module;

import java.util.List;

/**
 * Represents a module configuration in the Yue Discord Bot framework.
 * A module contains metadata about its functionality and compatibility.
 */
public class Module {
	/**
	 * The name of the module
	 */
	private final String name;

	/**
	 * The version string of the module
	 */
	private final String version;

	/**
	 * List of module authors
	 */
	private final List<String> authors;

	/**
	 * List of game versions this module supports
	 */
	final List<String> supportedVersions;

	/**
	 * The main class path of the module
	 */
	private final String main;

	/**
	 * Constructs a new Module instance with no specific values.
	 * This constructor is typically used for deserialization purposes.
	 */
	public Module() {
		this.name = null;
		this.version = null;
		this.authors = null;
		this.supportedVersions = null;
		this.main = null;
	}

	/**
	 * Constructs a new Module instance with the specified parameters.
	 *
	 * @param name              The name of the module
	 * @param version           The version string of the module
	 * @param authors           List of module authors
	 * @param supportedVersions List of game versions this module supports
	 * @param main              The main class path of the module
	 */
	public Module(String name, String version, List<String> authors, List<String> supportedVersions, String main) {
		this.name = name;
		this.version = version;
		this.authors = authors;
		this.supportedVersions = supportedVersions;
		this.main = main;
	}

	/**
	 * Gets the name of the module.
	 *
	 * @return The name of the module
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the version string of the module.
	 *
	 * @return The version string of the module
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Gets the list of module authors.
	 *
	 * @return List of module authors
	 */
	public List<String> getAuthors() {
		return authors;
	}

	/**
	 * Gets the list of game versions this module supports.
	 *
	 * @return List of supported game versions
	 */
	public List<String> getSupportedVersions() {
		return supportedVersions;
	}

	/**
	 * Gets the main class path of the module.
	 *
	 * @return The main class path of the module
	 */
	public String getMain() {
		return main;
	}
}