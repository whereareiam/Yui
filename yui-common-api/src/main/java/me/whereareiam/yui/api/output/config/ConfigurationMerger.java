package me.whereareiam.yui.api.output.config;

/**
 * Interface for merging configuration objects in the Yui Discord Bot framework.
 * Provides functionality to merge userprofile configurations with default configurations,
 * ensuring that missing values are populated with defaults while preserving
 * userprofile-defined settings.
 */
public interface ConfigurationMerger {
	/**
	 * Merges a configuration object with its default configuration.
	 *
	 * @param config        the userprofile configuration to be updated
	 * @param defaultConfig the default configuration to merge from
	 * @param <T>           the type of configuration class
	 */
	<T> void merge(T config, T defaultConfig);
}