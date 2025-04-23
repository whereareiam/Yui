package me.whereareiam.yue.common.listener;

import lombok.AllArgsConstructor;
import me.whereareiam.yue.common.service.initialization.UserProfileInitializationService;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GuildMemberJoinListener extends ListenerAdapter {
	private final UserProfileInitializationService initializer;

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		initializer.initializeForUser(event.getUser().getIdLong());
	}
}
