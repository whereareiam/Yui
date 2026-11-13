package me.whereareiam.yui.api.event.fluctlight.language;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.interactions.DiscordLocale;

/**
 * Published after a language was added to those a user speaks besides their primary one.
 * <p>
 * Listen with {@link org.springframework.transaction.event.TransactionalEventListener} to receive it once the change
 * is committed. A failing listener is logged and does not undo the change.
 */
@Getter
@RequiredArgsConstructor
public class FluctlightAdditionalLanguageAddedEvent {
	/**
	 * The user after the change.
	 */
	private final Fluctlight fluctlight;

	/**
	 * The added language.
	 */
	private final DiscordLocale language;
}
