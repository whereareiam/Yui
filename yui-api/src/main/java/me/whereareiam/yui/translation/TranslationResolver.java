package me.whereareiam.yui.translation;

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
 * String result = TranslationResolver.resolve("translate(vocabulary.cancel)", locale);
 * 
 * // Plain text
 * String result = TranslationResolver.resolve("Cancel", locale);
 * 
 * // With user ID
 * String result = TranslationResolver.resolve("translate(vocabulary.cancel)", userId);
 * }</pre>
 */
public final class TranslationResolver {
    private static final String TRANSLATE_PREFIX = "translate(";
    private static final String TRANSLATE_SUFFIX = ")";

    private TranslationResolver() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Resolves text that can be either a translation key or plain text.
     * <p>
     * If text starts with {@code translate(} and ends with {@code )}, extracts the key
     * and translates it using the provided locale. Otherwise, returns the text as-is.
     *
     * @param text   The text to resolve (can be "translate(key)" or plain text)
     * @param locale The locale to use for translation
     * @return The resolved text (translated or plain)
     */
    public static String resolve(String text, DiscordLocale locale) {
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
     * Resolves text that can be either a translation key or plain text.
     * <p>
     * If text starts with {@code translate(} and ends with {@code )}, extracts the key
     * and translates it for the specified user. Otherwise, returns the text as-is.
     *
     * @param text   The text to resolve (can be "translate(key)" or plain text)
     * @param userId The user ID to resolve locale for
     * @return The resolved text (translated or plain)
     */
    public static String resolve(String text, long userId) {
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
     * Resolves text using the default locale.
     *
     * @param text The text to resolve (can be "translate(key)" or plain text)
     * @return The resolved text (translated or plain)
     */
    public static String resolve(String text) {
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
     * Checks if the given text is a translation key pattern.
     *
     * @param text The text to check
     * @return true if text matches "translate(key)" pattern
     */
    public static boolean isTranslationKey(String text) {
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
