package me.whereareiam.yui.common.listener.guild;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.fluctlight.FluctlightRegistry;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GuildMemberRemoveListener extends ListenerAdapter {
	private final FluctlightRegistry fluctlightRegistry;

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		long userId = event.getUser().getIdLong();

		fluctlightRegistry.evictFluctlight(userId);
	}
}
