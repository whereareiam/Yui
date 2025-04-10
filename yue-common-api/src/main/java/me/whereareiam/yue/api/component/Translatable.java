package me.whereareiam.yue.api.component;

import me.whereareiam.yue.api.input.translation.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provides a static method to quickly translate a key for a user:
 * String text = Translatable.of("module.music.play_button", userId);
 * <p>
 * Under the hood, it calls the TranslationService via a static reference.
 * Note: static injection is generally not recommended, but we do it
 * here to meet the requirement of a single-line usage.
 */
@Component
public class Translatable {

	private static TranslationService translationService;

	/**
	 * This setter is automatically called by Spring to inject the service bean.
	 */
	@Autowired
	public void setTranslationService(TranslationService translationService) {
		Translatable.translationService = translationService;
	}

	/**
	 * Quick usage:
	 * String label = Translatable.of("module.music.play_button", userId);
	 */
	public static String of(String key, long userId) {
		if (translationService == null) {
			// fallback or throw an exception if service not injected
			return key;
		}
		return translationService.translate(key, userId);
	}

	// Private constructor => no instances
	private Translatable() {
	}
}