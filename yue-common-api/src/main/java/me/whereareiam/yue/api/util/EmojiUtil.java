package me.whereareiam.yue.api.util;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.Map;

@SuppressWarnings("unused")
public final class EmojiUtil {

	/**
	 * Default mapping when the locale tag has no explicit region part
	 */
	private static final Map<String, String> LANGUAGE_TO_REGION = Map.ofEntries(
			Map.entry("bg", "BG"), // Bulgarian
			Map.entry("hr", "HR"), // Croatian
			Map.entry("cs", "CZ"), // Czech
			Map.entry("da", "DK"), // Danish
			Map.entry("nl", "NL"), // Dutch
			Map.entry("fi", "FI"), // Finnish
			Map.entry("fr", "FR"), // French
			Map.entry("de", "DE"), // German
			Map.entry("el", "GR"), // Greek
			Map.entry("hi", "IN"), // Hindi
			Map.entry("hu", "HU"), // Hungarian
			Map.entry("id", "ID"), // Indonesian
			Map.entry("it", "IT"), // Italian
			Map.entry("ja", "JP"), // Japanese
			Map.entry("ko", "KR"), // Korean
			Map.entry("lt", "LT"), // Lithuanian
			Map.entry("no", "NO"), // Norwegian
			Map.entry("pl", "PL"), // Polish
			Map.entry("ro", "RO"), // Romanian
			Map.entry("ru", "RU"), // Russian
			Map.entry("sv", "SE"), // Swedish
			Map.entry("th", "TH"), // Thai
			Map.entry("tr", "TR"), // Turkish
			Map.entry("uk", "UA"), // Ukrainian
			Map.entry("vi", "VN")  // Vietnamese
	);

	private EmojiUtil() {
	}

	/**
	 * Converts a {@link DiscordLocale} to a Unicode flag wrapped into a JDA {@link Emoji}.
	 *
	 * @param locale Discord locale (must not be {@code null})
	 * @return matching {@link Emoji}, or ❓ when no reasonable flag is available
	 */
	public static Emoji of(DiscordLocale locale) {
		// locale tags are guaranteed non-null by the enum
		String localeTag = locale.getLocale();     // e.g. "en-US", "de", "pt-BR", "unknown"
		String region = extractRegion(localeTag);  // 2-letter ISO country code or null

		if (region != null) {
			return Emoji.fromUnicode(toFlagEmoji(region));
		}

		// Fallback: unknown locale → ❓
		return Emoji.fromUnicode("❓");
	}

	/**
	 * Extracts a 2-letter region code from a locale tag
	 * or consults the LANGUAGE_TO_REGION map.
	 */
	private static String extractRegion(String localeTag) {
		int sep = localeTag.indexOf('-');
		if (sep >= 0) {
			String part = localeTag.substring(sep + 1);
			// Only accept classic 2-letter ISO regions
			if (part.length() == 2 && isAsciiLetters(part)) {
				return part.toUpperCase();
			}
		}
		// No explicit region – try language default
		return LANGUAGE_TO_REGION.get(localeTag);
	}

	/**
	 * Checks that the string consists solely of basic Latin letters.
	 */
	private static boolean isAsciiLetters(String s) {
		for (char c : s.toCharArray()) {
			if (c < 'A' || (c > 'Z' && c < 'a') || c > 'z')
				return false;
		}
		return true;
	}

	/**
	 * Converts a 2-letter country code (e.g. {@code "DE"}) to its flag emoji
	 * by combining the two corresponding regional-indicator symbols.
	 */
	private static String toFlagEmoji(String countryCode) {
		int first = countryCode.charAt(0) - 'A' + 0x1F1E6;
		int second = countryCode.charAt(1) - 'A' + 0x1F1E6;
		return new String(Character.toChars(first)) +
				new String(Character.toChars(second));
	}
}