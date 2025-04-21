package me.whereareiam.yue.api.util;

import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces every literal {@code translate(key)} substring with the translated
 * value.  The replacement is performed recursively, so a translated value
 * that itself contains further {@code translate(…)} tokens will be expanded
 * until no tokens remain (or a hard recursion limit is hit).
 * <p>
 * Example:
 * raw:  "translate(vocabulary.cancel) / translate(vocabulary.help)"
 * out:  "Cancel / Help"
 * <p>
 * This class is intentionally dependency‑free; it relies only on the public
 * {@link Translatable} facade that already exists in the API layer.
 */
public final class TranslationTags {
	private static final Pattern TAG =
			Pattern.compile("translate\\(([^)]+)\\)");

	public static String resolve(String text, long userId) {
		return resolve(text, k -> Translatable.of(k, userId));
	}

	public static String resolve(String text, DiscordLocale locale) {
		return resolve(text, k -> Translatable.of(k, locale));
	}

	private interface Resolver {
		String apply(String key);
	}

	private static String resolve(String input, Resolver resolver) {
		if (input == null || input.isEmpty())
			return input;

		String result = input;
		int limiter = 0;
		while (limiter++ < 10) {
			Matcher m = TAG.matcher(result);
			if (!m.find())
				break;

			StringBuilder sb = new StringBuilder();

			do {
				String key = m.group(1).trim();
				String replacement = resolver.apply(key);
				m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
			} while (m.find());

			m.appendTail(sb);
			result = sb.toString();
		}

		return result;
	}
}
