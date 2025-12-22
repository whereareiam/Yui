package me.whereareiam.yui.event.fluctlight.language;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.whereareiam.yui.event.Cancellable;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.interactions.DiscordLocale;

/**
 * Cancellable event published BEFORE an additional language is removed.
 * Listeners can cancel this event to prevent the language removal.
 */
@Getter
@Setter
@RequiredArgsConstructor
public class FluctlightAdditionalLanguageRemovedEvent implements Cancellable {
	private final Fluctlight fluctlight;
	private DiscordLocale language;
	private boolean cancelled;
}
