package me.whereareiam.yui.api.output.config;

import me.whereareiam.yui.api.exception.ConfigLoadException;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * Interface for loading configuration files in the Yui Discord Bot framework.
 * Provides methods to load configuration from both file paths and input streams,
 * deserializing them into specified Java objects.
 */
public interface ConfigurationLoader {
	/**
	 * Loads and deserializes a configuration file into the specified class type.
	 *
	 * @param path  the path to the configuration file
	 * @param clazz the class type to deserialize into
	 * @param <T>   the type of configuration class
	 * @return the deserialized configuration object
	 * @throws ConfigLoadException if loading or parsing fails
	 */
	<T> T load(Path path, Class<T> clazz);

	/**
	 * Loads and deserializes a configuration from an input stream into the specified class type.
	 *
	 * @param stream the input stream containing configuration data
	 * @param clazz  the class type to deserialize into
	 * @param <T>    the type of configuration class
	 * @return the deserialized configuration object
	 * @throws ConfigLoadException if loading or parsing fails
	 */
	<T> T load(InputStream stream, Class<T> clazz);

	/**
	 * Loads and deserializes a configuration file into the specified class type,
	 * providing a default configuration object in case of failure.
	 *
	 * @param path          the path to the configuration file
	 * @param clazz         the class type to deserialize into
	 * @param defaultConfig the default configuration object to return if loading fails
	 * @param <T>           the type of configuration class
	 * @return the deserialized configuration object or the default configuration
	 */
	<T> T load(Path path, Class<T> clazz, DefaultConfig<T> defaultConfig);
}