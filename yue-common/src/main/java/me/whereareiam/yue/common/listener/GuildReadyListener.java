package me.whereareiam.yue.common.listener;

import me.whereareiam.yue.api.output.provider.UserProfileCacheProvider;
import me.whereareiam.yue.api.output.service.UserProfileService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GuildReadyListener extends ListenerAdapter {
	private final UserProfileCacheProvider userProfileCacheProvider;
	private final UserProfileService userProfileService;

	@Autowired
	public GuildReadyListener(UserProfileCacheProvider userProfileCacheProvider, UserProfileService userProfileService) {
		this.userProfileCacheProvider = userProfileCacheProvider;
		this.userProfileService = userProfileService;
	}

	@Override
	public void onGuildReady(GuildReadyEvent event) {
		Guild guild = event.getGuild();

		guild.loadMembers().onSuccess(members -> {
			for (Member member : members) {
				long userId = member.getIdLong();

				userProfileService.getProfile(userId).ifPresentOrElse(
						profile -> userProfileCacheProvider.putProfile(userId, profile),
						() -> {
							userProfileService.createProfile(userId).ifPresent(newProfile ->
									userProfileCacheProvider.putProfile(userId, newProfile)
							);
						}
				);
			}
		});
	}
}
