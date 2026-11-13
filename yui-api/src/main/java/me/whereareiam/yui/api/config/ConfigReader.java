package me.whereareiam.yui.api.config;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;

/**
 * Reads configuration the way Yui reads its own: YAML with Yui's value types (durations such as {@code 1d}, colours,
 * locales), environment variables in strings ({@code ${TOKEN}}) and a single value where a list is expected. A file is
 * never written; what it leaves out takes the defaults.
 *
 * <pre>{@code
 * Settings settings = reader.read(folder.resolve("settings"), new Settings());
 * Map<String, Topic> topics = reader.readMap(folder.resolve("topics"), Topic.class);
 * Requirements requires = reader.parse("- audience: moderator", Requirements.class);
 * }</pre>
 */
public interface ConfigReader {
	/**
	 * @param file     the file, without its extension
	 * @param defaults the values a missing setting takes, also returned whole when there is no such file
	 * @param <T>      the model the file is written in
	 * @return the file's content, completed by the defaults
	 */
	<T> @NotNull T read(@NotNull Path file, @NotNull T defaults);

	/**
	 * Reads a file whose top level is a map, entries the installation names itself ({@code development:},
	 * {@code minecraft:}), each read as the value type.
	 *
	 * @param file  the file, without its extension
	 * @param value the type of each entry
	 * @param <V>   the entries' type
	 * @return the entries by key, in the file's order; empty when there is no such file
	 * @throws IllegalArgumentException when an entry does not fit the type
	 */
	<V> @NotNull Map<String, V> readMap(@NotNull Path file, @NotNull Class<V> value);

	/**
	 * Reads a value written in code, such as a default list of requirements.
	 *
	 * @param yaml the value as YAML
	 * @param type the type to read it as
	 * @param <T>  the type
	 * @return the value
	 * @throws IllegalArgumentException when the YAML does not fit the type
	 */
	<T> @NotNull T parse(@NotNull String yaml, @NotNull Class<T> type);
}
