package me.whereareiam.yui.common.message;

import lombok.RequiredArgsConstructor;
import me.whereareiam.semantica.Text;
import me.whereareiam.semantica.common.Semantica;
import me.whereareiam.yui.message.TextService;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Resolves texts through Semantica, in a Discord locale, rendered for Discord.
 */
@Service
@RequiredArgsConstructor
public class SemanticaTextService implements TextService {
	private final Semantica semantica;

	@Override
	public @NotNull String resolve(@NotNull Text text, @NotNull DiscordLocale locale) {
		Locale reader = locale == DiscordLocale.UNKNOWN ? DefaultsSource.ENGLISH : Locale.forLanguageTag(locale.getLocale());

		return DiscordRenderer.render(semantica.resolve(text, reader));
	}
}
