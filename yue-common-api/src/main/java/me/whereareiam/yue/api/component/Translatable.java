package me.whereareiam.yue.api.component;

import me.whereareiam.yue.api.input.translation.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Utility component that provides static access to translations.
 * <p>
 * This class serves as a convenient wrapper around the {@link TranslationService},
 * allowing for easy access to translations from anywhere in the application without
 * needing to inject the service directly.
 * <p>
 * Usage examples:
 * <pre>{@code
 * // Translate for a specific user
 * String cancelText = Translatable.of("vocabulary.cancel", userId);
 *
 * // Translate using default locale
 * String helpText = Translatable.of("vocabulary.help");
 * }</pre>
 * <p>
 * The class handles the case when the translation service is not yet initialized
 * by returning the original key.
 *
 * @see TranslationService
 */
@Component
public class Translatable {

	private static TranslationService translationService;

	/**
	 * Sets the translation service instance.
	 * <p>
	 * This method is called automatically by Spring during component initialization.
	 *
	 * @param translationService The translation service to use
	 */
	@Autowired
	public void setTranslationService(TranslationService translationService) {
		Translatable.translationService = translationService;
	}

	/**
	 * Translates a key for a specific user.
	 *
	 * @param key    The translation key to look up
	 * @param userId The ID of the user for whom to translate
	 * @return The translated string or the original key if translation is unavailable
	 */
	public static String of(String key, long userId) {
		if (translationService == null) {
			return key;
		}
		return translationService.translate(key, userId);
	}

	/**
	 * Translates a key using the default locale.
	 * <p>
	 * This is equivalent to calling {@code of(key, 0)}.
	 *
	 * @param key The translation key to look up
	 * @return The translated string or the original key if translation is unavailable
	 */
	public static String of(String key) {
		if (translationService == null) {
			return key;
		}
		return translationService.translate(key, 0);
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private Translatable() {
	}
}