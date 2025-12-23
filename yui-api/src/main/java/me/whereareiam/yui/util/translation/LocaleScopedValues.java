package me.whereareiam.yui.util.translation;

import me.whereareiam.configura.type.MultiValue;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Utility for resolving locale-scoped values from configuration entries.
 * Accepts entries in the form:
 * - {@code "<channelId>"} (applies to any locale)
 * - {@code "<locale>:<channelId>"} (applies only to the specified locale, e.g., "ru:123")
 * <p>
 * If entries for the requested locale exist, they are returned. Otherwise, locale-agnostic
 * entries are returned. Blank/null entries are ignored.
 */
public final class LocaleScopedValues {
	public static List<String> resolve(MultiValue<String> values, DiscordLocale locale) {
		if (values == null) return List.of();
		return resolve(values.asList(), locale);
	}

	/**
	 * Extract locales present as prefixes in the values (e.g., "ru:123" -> ru).
	 *
	 * @param values Collection of raw entries
	 * @return Ordered, unique locales found in prefixes
	 */
	public static List<DiscordLocale> extractLocales(Collection<String> values) {
		if (values == null || values.isEmpty()) return List.of();

		var locales = new java.util.LinkedHashSet<DiscordLocale>();
		for (String raw : values) {
			if (raw == null) continue;
			int idx = raw.indexOf(':');
			if (idx <= 0) continue;

			String prefix = raw.substring(0, idx);
			DiscordLocale parsed = DiscordLocale.from(prefix.replace('_', '-'));
			if (parsed != DiscordLocale.UNKNOWN)
				locales.add(parsed);
		}

		return List.copyOf(locales);
	}

	public static List<String> resolve(Collection<String> values, DiscordLocale locale) {
		if (values == null || values.isEmpty()) return List.of();

		List<String> defaults = new ArrayList<>();
		List<String> localeSpecific = new ArrayList<>();

		for (String raw : values) {
			if (raw == null || raw.isBlank()) continue;

			int idx = raw.indexOf(':');
			if (idx > 0) {
				String prefix = raw.substring(0, idx);
				String payload = raw.substring(idx + 1);
				DiscordLocale parsed = DiscordLocale.from(prefix);
				if (parsed != DiscordLocale.UNKNOWN && parsed.equals(locale)) {
					if (!payload.isBlank())
						localeSpecific.add(payload);
					continue;
				}
			}

			defaults.add(raw);
		}

		if (locale != null && !localeSpecific.isEmpty())
			return localeSpecific;

		return defaults;
	}
}
