package me.whereareiam.yui.api.message;

import org.jetbrains.annotations.NotNull;

/**
 * One file of default texts, in English: Yui's or a plugin's. Its {@link #file()} is the file's path inside a locale
 * folder of {@code messages/} and the prefix of its keys, so the defaults of {@code commands} give the keys
 * {@code commands.*}, and an administrator overrides them in {@code messages/<locale>/commands.yml}. A plugin's keys
 * also start with its id.
 *
 * <pre>{@code
 * @Component
 * public class JourneyMessagesDefaults implements MessageDefaults<JourneyMessages> {
 *     public String file() { return "journey"; }
 *     public JourneyMessages defaults() { ... }
 * }
 * }</pre>
 *
 * <p>Texts are ICU messages: {@code "Deleted {count, plural, one {# message} other {# messages}}."}</p>
 *
 * @param <T> the model the texts are written in, read as Configura reads it
 */
public interface MessageDefaults<T> {
	/**
	 * @return the file's path inside a locale folder, without extension, such as {@code commands}
	 */
	@NotNull String file();

	/**
	 * @return the default texts
	 */
	@NotNull T defaults();
}
