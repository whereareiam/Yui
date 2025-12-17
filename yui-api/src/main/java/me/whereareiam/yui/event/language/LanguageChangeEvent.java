package me.whereareiam.yui.event.language;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.whereareiam.yui.event.Cancellable;
import net.dv8tion.jda.api.interactions.DiscordLocale;

@Getter
@Setter
@RequiredArgsConstructor
public class LanguageChangeEvent implements Cancellable {
	private final long user;
	private final DiscordLocale oldLanguage;
	private DiscordLocale language;
	private boolean cancelled;
}
