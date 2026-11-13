package me.whereareiam.yui.interests.listener;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.interests.service.TopicPicks;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * A member who rejoins gets the roles of their picks back.
 */
@Component
@RequiredArgsConstructor
public class MemberListener extends ListenerAdapter {
	private final TopicPicks picks;

	@Override
	public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
		picks.restore(event.getUser().getIdLong());
	}
}
