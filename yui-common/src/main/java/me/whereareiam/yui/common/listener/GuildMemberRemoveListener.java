package me.whereareiam.yui.common.listener;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.registry.UserProfileCacheRegistry;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GuildMemberRemoveListener extends ListenerAdapter {
	private final UserProfileCacheRegistry userProfileCacheRegistry;

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		long userId = event.getUser().getIdLong();

		userProfileCacheRegistry.evictProfile(userId);
	}
}
