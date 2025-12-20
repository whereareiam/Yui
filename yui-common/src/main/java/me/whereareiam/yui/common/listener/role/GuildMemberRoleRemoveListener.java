package me.whereareiam.yui.common.listener.role;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.service.UserRoleService;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

@Component
@AllArgsConstructor
public class GuildMemberRoleRemoveListener extends ListenerAdapter {
	private final UserRoleService userRoleService;
	private final ExecutorService scheduledPool;

	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
		long userId = event.getUser().getIdLong();
		
		scheduledPool.execute(() -> userRoleService.syncUser(userId));
	}
}
