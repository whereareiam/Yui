package me.whereareiam.yue.api.input.translation;

/**
 * High-level interface for the translation system.
 * It merges both core (framework) and module translations under the hood.
 */
public interface TranslationService {

	/**
	 * Translates the given key for the specified user ID.
	 * <p>
	 * If the key starts with "module.XYZ.", we look in module XYZ's translations.
	 * Otherwise, we treat it as a core translation key (domain = "core").
	 *
	 * @param key    translation key, e.g. "module.music.play_button" or "greeting"
	 * @param userId the user ID for which we fetch the best language
	 * @return the translated text, or the key itself if none found
	 */
	String translate(String key, long userId);

	/**
	 * (Optionally) direct lookup by domain, subKey, and userId if you prefer more control.
	 */
	default String translate(String domain, String subKey, long userId) {
		// Typically you'd implement a direct method or just parse the domain from "module.XYZ."
		// We'll leave it as default if you don't want it mandatory.
		return translate(domain + "." + subKey, userId);
	}
}