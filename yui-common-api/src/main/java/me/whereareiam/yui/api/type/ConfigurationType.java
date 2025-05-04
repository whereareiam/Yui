package me.whereareiam.yui.api.type;

public enum ConfigurationType {
	/**
	 * YAML configuration format with .yml extension
	 */
	YAML(".yml"),

	/**
	 * JSON configuration format with .json extension
	 */
	JSON(".json");

	/**
	 * The file extension associated with this configuration type
	 */
	private final String extension;

	/**
	 * Constructs a ConfigurationType with the specified file extension
	 *
	 * @param extension the file extension for this configuration type
	 */
	ConfigurationType(String extension) {
		this.extension = extension;
	}

	/**
	 * Returns the file extension associated with this configuration type
	 *
	 * @return the file extension
	 */
	public String getExtension() {
		return extension;
	}
}