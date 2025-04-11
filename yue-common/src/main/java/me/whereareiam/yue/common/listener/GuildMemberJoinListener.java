package me.whereareiam.yue.common.listener;

import me.whereareiam.yue.api.output.provider.UserProfileCacheProvider;
import me.whereareiam.yue.api.output.service.UserProfileService;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GuildMemberJoinListener extends ListenerAdapter {
	private final UserProfileCacheProvider userProfileCacheProvider;
	private final UserProfileService userProfileService;

	@Autowired
	public GuildMemberJoinListener(UserProfileCacheProvider userProfileCacheProvider, UserProfileService userProfileService) {
		this.userProfileCacheProvider = userProfileCacheProvider;
		this.userProfileService = userProfileService;
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		long userId = event.getUser().getIdLong();

		userProfileService.getProfile(userId).ifPresentOrElse(
				profile -> userProfileCacheProvider.putProfile(userId, profile),
				() -> {
					// Optionally create a default userprofile here, then cache
				}
		);
	}
}
