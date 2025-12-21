package me.whereareiam.yui.translation;

import me.whereareiam.yui.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.interactions.DiscordLocale;

/**
 * Utility for resolving text that can be either a plain string or a translation key.
 * <p>
 * Supports the pattern: {@code translate(key)} to indicate a translation key,
 * or plain text for non-translatable strings.
 * <p>
 * Usage examples:
 * <pre>{@code
 * // Translatable text
 * String result = TranslationResolver.text("translate(vocabulary.cancel)").resolve(fluctlight);
 * 
 * // Plain text
 * String result = TranslationResolver.text("Cancel").resolve(fluctlight);
 * 
 * // With locale
 * String result = TranslationResolver.text("translate(vocabulary.cancel)").resolve(locale);
 * 
 * // With user ID
 * String result = TranslationResolver.text("translate(vocabulary.cancel)").resolve(userId);
 * }</pre>
 */
public final class TranslationResolver {
    private static final String TRANSLATE_PREFIX = "translate(";
    private static final String TRANSLATE_SUFFIX = ")";

    private final String text;

    private TranslationResolver(String text) {
        this.text = text;
    }

    /**
     * Start building a resolvable text.
     *
     * @param text The text to resolve (can be "translate(key)" or plain text)
     * @return A new TranslationResolver instance
     */
    public static TranslationResolver text(String text) {
        return new TranslationResolver(text);
    }

    /**
     * Resolves the text using the default locale.
     * <p>
     * If text starts with {@code translate(} and ends with {@code )}, extracts the key
     * and translates it. Otherwise, returns the text as-is.
     *
     * @return The resolved text (translated or plain)
     */
    public String resolveDefault() {
        if (text == null || text.isBlank()) {
            return text;
        }

        if (isTranslationKey(text)) {
            String key = extractKey(text);
            return Translatable.text(key).resolveDefault();
        }

        return text;
    }

    /**
     * Resolves the text for a specific fluctlight.
     *
     * @param fluctlight The fluctlight whose locale should be used
     * @return The resolved text (translated or plain)
     */
    public String resolve(Fluctlight fluctlight) {
        if (text == null || text.isBlank()) {
            return text;
        }

        if (isTranslationKey(text)) {
            String key = extractKey(text);
            return Translatable.text(key).resolve(fluctlight);
        }

        return text;
    }

    /**
     * Resolves the text for a specific user ID.
     *
     * @param userId The user ID to resolve locale for
     * @return The resolved text (translated or plain)
     */
    public String resolve(long userId) {
        if (text == null || text.isBlank()) {
            return text;
        }

        if (isTranslationKey(text)) {
            String key = extractKey(text);
            return Translatable.text(key).resolve(userId);
        }

        return text;
    }

    /**
     * Resolves the text using a specific locale.
     *
     * @param locale The locale to use for translation
     * @return The resolved text (translated or plain)
     */
    public String resolve(DiscordLocale locale) {
        if (text == null || text.isBlank()) {
            return text;
        }

        if (isTranslationKey(text)) {
            String key = extractKey(text);
            return Translatable.text(key).resolve(locale);
        }

        return text;
    }

    /**
     * Checks if the given text is a translation key pattern.
     *
     * @param text The text to check
     * @return true if text matches "translate(key)" pattern
     */
    private static boolean isTranslationKey(String text) {
        return text != null
                && text.startsWith(TRANSLATE_PREFIX)
                && text.endsWith(TRANSLATE_SUFFIX)
                && text.length() > TRANSLATE_PREFIX.length() + TRANSLATE_SUFFIX.length();
    }

    /**
     * Extracts the translation key from "translate(key)" pattern.
     *
     * @param text The text containing the pattern
     * @return The extracted key, or the original text if pattern doesn't match
     */
    private static String extractKey(String text) {
        if (!isTranslationKey(text)) {
            return text;
        }
        return text.substring(TRANSLATE_PREFIX.length(), text.length() - TRANSLATE_SUFFIX.length()).trim();
    }
}
