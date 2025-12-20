package me.whereareiam.yui.model.config.languages;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class Languages {
	private Map<String, LanguageEntry> languages;

	/**
	 * Converts languages map to use DiscordLocale keys.
	 * Filters out any invalid locales (UNKNOWN).
	 */
	public Map<DiscordLocale, LanguageEntry> toLocaleMap() {
		if (languages == null) return new HashMap<>();
		
		return languages.entrySet().stream()
				.filter(entry -> {
					DiscordLocale locale = DiscordLocale.from(entry.getKey());
					return locale != DiscordLocale.UNKNOWN;
				})
				.collect(Collectors.toMap(
						entry -> DiscordLocale.from(entry.getKey()),
						Map.Entry::getValue
				));
	}
}

