package me.whereareiam.yui.model.config.languages;

import me.whereareiam.configura.annotation.PreserveUnknownFields;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * {@code languages.yml}: the languages members may declare, keyed by Discord locale. A language is offered when it
 * is listed.
 *
 * <pre>{@code
 * _version: 1
 * en-US:
 *   emoji: "🇺🇸"
 *   role: "123456789012345678"
 * de:
 *   emoji: "🇩🇪"
 * }</pre>
 */
@PreserveUnknownFields
public class Languages {
	private final Map<DiscordLocale, Language> languages;

	public Languages() {
		this(Map.of());
	}

	/**
	 * @param languages the languages, in the order they are offered
	 */
	public Languages(@NotNull Map<DiscordLocale, Language> languages) {
		this.languages = Collections.unmodifiableMap(new LinkedHashMap<>(languages));
	}

	/**
	 * @return the listed languages in the file's order, unmodifiable
	 */
	public @NotNull Map<DiscordLocale, Language> all() {
		return languages;
	}

	/**
	 * @param locale a Discord locale
	 * @return the language listed for it, if any
	 */
	public @NotNull Optional<Language> get(@NotNull DiscordLocale locale) {
		return Optional.ofNullable(languages.get(locale));
	}
}
