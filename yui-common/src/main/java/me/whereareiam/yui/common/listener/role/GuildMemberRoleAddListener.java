package me.whereareiam.yui.common.listener.role;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.api.input.UserRoleService;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GuildMemberRoleAddListener extends ListenerAdapter {
	private final UserRoleService userRoleService;

	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
		long userId = event.getUser().getIdLong();

		userRoleService.syncUser(userId);
	}
}
