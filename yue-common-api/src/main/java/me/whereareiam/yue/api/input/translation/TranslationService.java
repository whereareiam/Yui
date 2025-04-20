package me.whereareiam.yue.api.input.translation;

import me.whereareiam.yue.api.Translatable;
import net.dv8tion.jda.api.interactions.DiscordLocale;

/**
 * Service responsible for translating string keys into localized text.
 * <p>
 * The translation service:
 * <ul>
 *   <li>Combines translations from multiple {@link TranslationLoader}s</li>
 *   <li>Resolves user-specific language preferences</li>
 *   <li>Falls back to default language when translations are missing</li>
 *   <li>Handles namespaced keys for core and plugin-specific translations</li>
 * </ul>
 * <p>
 * Translations are organized in a hierarchical structure where keys use dot notation
 * (e.g., "vocabulary.cancel" or "plugin.music.vocabulary.play").
 * <p>
 * This service is typically used by the {@link Translatable}
 * utility for convenient access to translations.
 *
 * @see TranslationLoader
 * @see Translatable
 */
public interface TranslationService {
	void initialize();

	/**
	 * Translates a key into localized text for a specific user.
	 * <p>
	 * The method attempts to find a translation based on the user's preferred languages
	 * (primary and additional). If no translation is found for the user's languages,
	 * it falls back to the default bot locale. If no translation exists at all,
	 * the original key is returned.
	 *
	 * @param key    The translation key to look up (e.g., "vocabulary.cancel")
	 * @param userId The ID of the user for whom to translate (determines language preferences)
	 * @return The translated string if found, or the original key if no translation exists
	 */
	String translate(String key, long userId);

	/**
	 * Translates a key into localized text using the specified Discord locale.
	 * <p>
	 * This method attempts to find a translation for the given key in the specified locale.
	 * If no translation is found for the specified locale, it falls back to the default bot locale.
	 * If no translation exists at all, the original key is returned.
	 * <p>
	 * This method is useful for translating messages that are not user-specific,
	 * such as general server messages or commands with locale options.
	 *
	 * @param key    The translation key to look up (e.g., "vocabulary.cancel")
	 * @param locale The Discord locale to use for translation
	 * @return The translated string if found, or the original key if no translation exists
	 */
	String translate(String key, DiscordLocale locale);
}