package me.whereareiam.yui.common.listener.role;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.api.input.UserRoleService;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

@Component
@AllArgsConstructor
public class GuildMemberRoleAddListener extends ListenerAdapter {
	private final UserRoleService userRoleService;
	private final ExecutorService syncPool;

	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
		long userId = event.getUser().getIdLong();

		// Skip if this user is already being synced by our bot
		if (userRoleService.isUserBeingSynced(userId))
			return;

		syncPool.execute(() -> userRoleService.syncUser(userId));
	}
}
