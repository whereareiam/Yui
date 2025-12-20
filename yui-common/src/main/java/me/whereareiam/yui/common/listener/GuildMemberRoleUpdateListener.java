package me.whereareiam.yui.common.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.service.RoleService;
import me.whereareiam.yui.fluctlight.FluctlightService;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuildMemberRoleUpdateListener extends ListenerAdapter {
	private final RoleService roleService;
	private final FluctlightService fluctlightService;

	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
		long userId = event.getUser().getIdLong();

		fluctlightService.get(userId).ifPresent(fluctlight -> {
			roleService.syncUserRoles(fluctlight);
			log.debug("Triggered role sync for user {} after role add", userId);
		});
	}

	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
		long userId = event.getUser().getIdLong();

		fluctlightService.get(userId).ifPresent(fluctlight -> {
			roleService.syncUserRoles(fluctlight);
			log.debug("Triggered role sync for user {} after role remove", userId);
		});
	}
}

