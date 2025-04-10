package me.whereareiam.yue.common.listener;

import me.whereareiam.yue.api.output.provider.UserProfileCacheProvider;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GuildMemberRemoveListener extends ListenerAdapter {
	private final UserProfileCacheProvider userProfileCacheProvider;

	@Autowired
	public GuildMemberRemoveListener(UserProfileCacheProvider userProfileCacheProvider) {
		this.userProfileCacheProvider = userProfileCacheProvider;
	}

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		long userId = event.getUser().getIdLong();
		userProfileCacheProvider.evictProfile(userId);
	}
}
