package me.whereareiam.yui.api.message;

import me.whereareiam.semantica.Text;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;

/**
 * Resolves texts: Yui's own and its plugins', from their defaults and from {@code messages/<locale>/}, in the reader's
 * locale, falling back along its chain to English. The result is ready for Discord: the values a text inserts are
 * shown as written, never read as markdown. Most code resolves through {@link Translatable}.
 */
public interface TextService {
	/**
	 * @param text   the key and its values
	 * @param locale the reader's locale
	 * @return the text for Discord; a missing key shows the key
	 */
	@NotNull String resolve(@NotNull Text text, @NotNull DiscordLocale locale);
}
