package me.whereareiam.yui.common.listener;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.service.UserRoleService;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GuildMemberJoinListener extends ListenerAdapter {
	private final UserRoleService userRoleService;

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		long userId = event.getUser().getIdLong();

		userRoleService.syncUser(userId);
	}
}
