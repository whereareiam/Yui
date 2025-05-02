package me.whereareiam.yue.common.listener;

import lombok.AllArgsConstructor;
import me.whereareiam.yue.api.input.UserRoleService;
import me.whereareiam.yue.common.service.initialization.UserProfileInitializationService;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GuildMemberJoinListener extends ListenerAdapter {
	private final UserProfileInitializationService initializer;
	private final UserRoleService userRoleService;

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		long userId = event.getUser().getIdLong();

		initializer.initializeForUser(userId);
		userRoleService.syncUser(userId);
	}
}
