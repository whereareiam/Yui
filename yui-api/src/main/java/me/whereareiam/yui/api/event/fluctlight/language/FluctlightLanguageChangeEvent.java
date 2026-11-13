package me.whereareiam.yui.api.event.fluctlight.language;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.whereareiam.yui.api.event.Cancellable;
import me.whereareiam.yui.api.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.interactions.DiscordLocale;

/**
 * Published before a user's primary language changes.
 * <p>
 * Published inside the change's transaction, before anything is saved. Listeners may only veto by cancelling it;
 * side effects belong to the event published after the change.
 */
@Getter
@RequiredArgsConstructor
public class FluctlightLanguageChangeEvent implements Cancellable {
	/**
	 * The user before the change.
	 */
	private final Fluctlight fluctlight;

	/**
	 * The new primary language.
	 */
	private final DiscordLocale language;

	@Setter
	private boolean cancelled;
}
