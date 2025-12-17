package me.whereareiam.yui.config;

/**
 * Interface for managing configuration serialization, deserialization, and templates
 * in the Yui Discord bot framework. This manager handles configuration type detection,
 * custom serializers/deserializers, and default configuration templates.
 */
@SuppressWarnings("unused")
public interface ConfigurationManager {

	/**
	 * Adds a custom deserializer for a specific class type.
	 *
	 * @param clazz        the class to register the deserializer for
	 * @param deserializer the deserializer instance
	 */
	void addDeserializer(Class<?> clazz, Object deserializer);

	/**
	 * Adds a custom serializer for a specific class type.
	 *
	 * @param clazz      the class to register the serializer for
	 * @param serializer the serializer instance
	 */
	void addSerializer(Class<?> clazz, Object serializer);

	/**
	 * Registers a default configuration template for a specific class type.
	 *
	 * @param clazz    the class type for the template
	 * @param template the default configuration template
	 */
	void addTemplate(Class<?> clazz, DefaultConfig<?> template);

	/**
	 * Retrieves the default configuration template for a specific class type.
	 *
	 * @param clazz the class type to get the template for
	 * @param <T>   the type of configuration class
	 * @return the default configuration template
	 */
	<T> DefaultConfig<T> getTemplate(Class<T> clazz);

	/**
	 * Retrieves the class type associated with a default configuration template.
	 *
	 * @param defaultConfig the default configuration template
	 * @return the class type associated with the template
	 */
	Class<?> getConfigClassFromDefaultConfig(DefaultConfig<?> defaultConfig);
}