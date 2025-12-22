package me.whereareiam.yui.event.fluctlight.language;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.interactions.DiscordLocale;

/**
 * Non-cancellable event published AFTER the primary language has been changed.
 * This event is published after successful persistence to the database.
 */
@Getter
@RequiredArgsConstructor
public class FluctlightLanguageChangedEvent {
	private final Fluctlight fluctlight;
	private final DiscordLocale oldLanguage;
	private final DiscordLocale newLanguage;
}
