package me.whereareiam.yui.util.translation;

import me.whereareiam.semantica.translation.TranslationService;
import me.whereareiam.yui.fluctlight.FluctlightService;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Fluent API for translations with named placeholder support.
 * <p>
 * This class provides a builder-style API for constructing translations with type-safe
 * named placeholders, making code more readable and maintainable.
 * <p>
 * Usage examples:
 * <pre>{@code
 * // Simple translation
 * String text = Translatable.text("vocabulary.cancel").resolve(fluctlight);
 *
 * // With named placeholders
 * String greeting = Translatable.text("greeting.welcome")
 *     .with("playerName", "Steve")
 *     .with("points", 1500)
 *     .resolve(fluctlight);
 *
 * // With default locale
 * String error = Translatable.text("error.generic").resolveDefault();
 *
 * // With specific locale
 * String msg = Translatable.text("message.info").resolve(DiscordLocale.GERMAN);
 * }</pre>
 */
@SuppressWarnings("unused")
public class Translatable {
	private static TranslationService<DiscordLocale> service;
	private static FluctlightService fluctlightService;
	private static ObjectProvider<Settings> settingsProvider;

	private final String key;
	private final Map<String, Object> placeholders;

	/**
	 * Component to initialize static dependencies.
	 */
	@Component
	static class Initializer {
		@Autowired
		public Initializer(
				TranslationService<DiscordLocale> service,
				FluctlightService fluctlightService,
				ObjectProvider<Settings> settingsProvider
		) {
			Translatable.service = service;
			Translatable.fluctlightService = fluctlightService;
			Translatable.settingsProvider = settingsProvider;
		}
	}

	private Translatable(String key) {
		this.key = key;
		this.placeholders = new HashMap<>();
	}

	private Translatable(String key, Map<String, Object> placeholders) {
		this.key = key;
		this.placeholders = new HashMap<>(placeholders);
	}

	/**
	 * Start building a translatable text with the given key.
	 *
	 * @param key The translation key
	 * @return A new Translatable builder
	 */
	public static Translatable text(String key) {
		return new Translatable(key);
	}

	/**
	 * Add a named placeholder to this translation.
	 * <p>
	 * Returns a new Translatable instance with the placeholder added.
	 * This ensures immutability and prevents placeholder leakage.
	 *
	 * @param name  The placeholder name (e.g., "playerName" for &lt;p:playerName&gt;)
	 * @param value The value to replace the placeholder with
	 * @return A new Translatable with the placeholder added
	 */
	public Translatable with(String name, Object value) {
		Map<String, Object> newPlaceholders = new HashMap<>(this.placeholders);
		newPlaceholders.put(name, value);
		return new Translatable(this.key, newPlaceholders);
	}

	/**
	 * Add multiple named placeholders at once.
	 * <p>
	 * Returns a new Translatable instance with the placeholders added.
	 * This ensures immutability and prevents placeholder leakage.
	 *
	 * @param placeholders Map of placeholder names to values
	 * @return A new Translatable with the placeholders added
	 */
	public Translatable with(Map<String, Object> placeholders) {
		Map<String, Object> newPlaceholders = new HashMap<>(this.placeholders);
		newPlaceholders.putAll(placeholders);
		return new Translatable(this.key, newPlaceholders);
	}

	/**
	 * Resolve the translation using the default locale.
	 *
	 * @return The translated text
	 */
	public String resolveDefault() {
		if (service == null) return key;
		DiscordLocale locale = settingsProvider.getObject().getLocale();
		if (placeholders.isEmpty()) {
			return service.resolve(key, locale);
		}
		return service.resolve(key, locale, placeholders);
	}

	/**
	 * Resolve the translation for a specific fluctlight.
	 *
	 * @param fluctlight The fluctlight whose locale should be used
	 * @return The translated text
	 */
	public String resolve(Fluctlight fluctlight) {
		if (service == null) return key;
		return resolve(fluctlight.getId());
	}

	/**
	 * Resolve the translation for a specific user ID.
	 *
	 * @param userId The user ID whose locale should be used
	 * @return The translated text
	 */
	public String resolve(long userId) {
		if (service == null) return key;
		DiscordLocale locale = resolveLocale(userId);
		if (placeholders.isEmpty()) {
			return service.resolve(key, locale);
		}
		return service.resolve(key, locale, placeholders);
	}

	/**
	 * Resolve the translation using a specific locale.
	 *
	 * @param locale The locale to use
	 * @return The translated text
	 */
	public String resolve(DiscordLocale locale) {
		if (service == null) return key;
		if (placeholders.isEmpty()) {
			return service.resolve(key, locale);
		}
		return service.resolve(key, locale, placeholders);
	}

	private static DiscordLocale resolveLocale(long userId) {
		return fluctlightService.get(userId)
				.map(fluctlight -> {
					if (fluctlight.getPrimaryLanguage() != null) {
						return fluctlight.getPrimaryLanguage();
					}
					return settingsProvider.getObject().getLocale();
				})
				.orElseGet(() -> settingsProvider.getObject().getLocale());
	}
}