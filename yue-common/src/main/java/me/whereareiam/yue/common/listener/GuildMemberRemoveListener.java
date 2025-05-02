package me.whereareiam.yue.common.listener;

import lombok.AllArgsConstructor;
import me.whereareiam.yue.api.output.provider.UserProfileCacheProvider;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GuildMemberRemoveListener extends ListenerAdapter {
	private final UserProfileCacheProvider userProfileCacheProvider;

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		long userId = event.getUser().getIdLong();

		userProfileCacheProvider.evictProfile(userId);
	}
}
