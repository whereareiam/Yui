package me.whereareiam.yue.api.event.language;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.whereareiam.yue.api.event.Cancellable;
import net.dv8tion.jda.api.interactions.DiscordLocale;

@Getter
@Setter
@RequiredArgsConstructor
public class AdditionalLanguageRemovedEvent implements Cancellable {
	private final long user;
	private DiscordLocale language;
	private boolean cancelled;
}
