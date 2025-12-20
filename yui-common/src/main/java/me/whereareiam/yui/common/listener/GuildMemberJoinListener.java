package me.whereareiam.yui.common.listener;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.service.RoleService;
import me.whereareiam.yui.fluctlight.FluctlightService;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GuildMemberJoinListener extends ListenerAdapter {
	private final RoleService roleService;
	private final FluctlightService fluctlightService;

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		long userId = event.getUser().getIdLong();

		fluctlightService.get(userId).ifPresent(roleService::syncUserRoles);
	}
}
