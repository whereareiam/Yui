package me.whereareiam.yui.api.event.fluctlight.language;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.whereareiam.yui.api.event.Cancellable;
import me.whereareiam.yui.api.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.interactions.DiscordLocale;

/**
 * Published before a language is removed from those a user speaks besides their primary one.
 * <p>
 * Published inside the change's transaction, before anything is saved. Listeners may only veto by cancelling it;
 * side effects belong to the event published after the change.
 */
@Getter
@RequiredArgsConstructor
public class FluctlightAdditionalLanguageRemoveEvent implements Cancellable {
	/**
	 * The user before the change.
	 */
	private final Fluctlight fluctlight;

	/**
	 * The language to remove.
	 */
	private final DiscordLocale language;

	@Setter
	private boolean cancelled;
}
