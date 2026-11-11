package me.whereareiam.yui.event.fluctlight.language;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.interactions.DiscordLocale;

/**
 * Published after a user's primary language changed.
 * <p>
 * Listen with {@link org.springframework.transaction.event.TransactionalEventListener} to receive it once the change
 * is committed. A failing listener is logged and does not undo the change.
 */
@Getter
@RequiredArgsConstructor
public class FluctlightLanguageChangedEvent {
	/**
	 * The user after the change.
	 */
	private final Fluctlight fluctlight;

	/**
	 * The primary language before the change.
	 */
	private final DiscordLocale previousLanguage;
}
