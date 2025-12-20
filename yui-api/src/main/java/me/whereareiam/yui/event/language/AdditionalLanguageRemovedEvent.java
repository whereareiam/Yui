package me.whereareiam.yui.event.language;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.whereareiam.yui.event.Cancellable;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.interactions.DiscordLocale;

@Getter
@Setter
@RequiredArgsConstructor
public class AdditionalLanguageRemovedEvent implements Cancellable {
	private final Fluctlight fluctlight;
	private DiscordLocale language;
	private boolean cancelled;
}
