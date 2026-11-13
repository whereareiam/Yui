package me.whereareiam.yui.message;

import me.whereareiam.semantica.Text;
import me.whereareiam.yui.fluctlight.FluctlightService;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * A text by key, with the values of its arguments, resolved in a reader's locale: a member's primary language, or
 * Yui's default locale.
 *
 * <pre>{@code
 * Translatable.text("conversations.close.description").with("seconds", 30).resolve(member);
 * }</pre>
 */
@SuppressWarnings("unused")
public class Translatable {
	private static TextService service;
	private static FluctlightService fluctlightService;
	private static ObjectProvider<Settings> settingsProvider;

	private final Text text;

	@Component
	static class Initializer {
		@Autowired
		public Initializer(TextService service, FluctlightService fluctlightService, ObjectProvider<Settings> settingsProvider) {
			Translatable.service = service;
			Translatable.fluctlightService = fluctlightService;
			Translatable.settingsProvider = settingsProvider;
		}
	}

	private Translatable(Text text) {
		this.text = text;
	}

	/**
	 * @param key the text's key, such as {@code commands.clear.success.title}
	 * @return the text without values
	 */
	public static Translatable text(String key) {
		return new Translatable(Text.of(key));
	}

	/**
	 * @param name  an argument's name, as the message writes it ({@code {seconds}})
	 * @param value its value: text, a number, a date, an enum constant or a {@link Text}
	 * @return the text with that value
	 */
	public Translatable with(String name, Object value) {
		return new Translatable(text.with(name, value == null ? "" : value));
	}

	/**
	 * @param values arguments by name; a null value is shown as nothing
	 * @return the text with those values
	 */
	public Translatable with(Map<String, ?> values) {
		Text next = text;
		for (Map.Entry<String, ?> value : values.entrySet())
			next = next.with(value.getKey(), value.getValue() == null ? "" : value.getValue());

		return new Translatable(next);
	}

	/**
	 * @return the text in Yui's default locale
	 */
	public String resolveDefault() {
		return resolve(defaultLocale());
	}

	/**
	 * @return the text in the member's primary language
	 */
	public String resolve(Fluctlight fluctlight) {
		return resolve(fluctlight.getPrimaryLanguage() != null ? fluctlight.getPrimaryLanguage() : defaultLocale());
	}

	/**
	 * @return the text in the language of the user with that id
	 */
	public String resolve(long userId) {
		if (fluctlightService == null) return resolveDefault();

		return fluctlightService.get(userId).map(this::resolve).orElseGet(this::resolveDefault);
	}

	/**
	 * @return the text in that locale
	 */
	public String resolve(DiscordLocale locale) {
		if (service == null) return text.getKey();

		return service.resolve(text, locale);
	}

	private static DiscordLocale defaultLocale() {
		Settings settings = settingsProvider == null ? null : settingsProvider.getIfAvailable();

		return settings == null ? DiscordLocale.ENGLISH_US : settings.getLocale();
	}
}
