package me.whereareiam.yui.common.listener.guild;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.common.role.sync.RoleSyncScheduler;
import me.whereareiam.yui.fluctlight.FluctlightService;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

/**
 * Listens for new members joining the guild and syncs their roles.
 * <p>
 * When a member joins, we force an immediate sync to ensure they get
 * the correct roles based on their Fluctlight state.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GuildMemberJoinListener extends ListenerAdapter {
	private final RoleSyncScheduler roleSyncScheduler;
	private final FluctlightService fluctlightService;

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		long userId = event.getUser().getIdLong();

		fluctlightService.get(userId).ifPresent(fluctlight -> {
			roleSyncScheduler.forceSyncNow(fluctlight);
			log.debug("Force synced roles for newly joined member: {}", userId);
		});
	}
}
