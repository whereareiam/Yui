package me.whereareiam.yui.common.listener.guild;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.common.fluctlight.FluctlightCache;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GuildMemberRemoveListener extends ListenerAdapter {
	private final FluctlightCache fluctlightCache;

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		fluctlightCache.invalidate(event.getUser().getIdLong());
	}
}
