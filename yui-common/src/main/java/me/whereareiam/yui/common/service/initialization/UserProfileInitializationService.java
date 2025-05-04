package me.whereareiam.yui.common.service.initialization;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.api.output.provider.UserProfileCacheProvider;
import me.whereareiam.yui.api.output.service.UserProfileService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserProfileInitializationService {
	private final UserProfileService userProfileService;
	private final UserProfileCacheProvider cacheProvider;
	private final JDA jda;

	@Order(Integer.MIN_VALUE)
	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		jda.getGuilds().forEach(this::initializeForGuild);
	}

	public void initializeForUser(long userId) {
		userProfileService.getProfile(userId)
				.ifPresentOrElse(
						profile -> cacheProvider.putProfile(userId, profile),
						() -> userProfileService
								.createProfile(userId)
								.ifPresent(profile -> cacheProvider.putProfile(userId, profile))
				);
	}

	public void initializeForGuild(Guild guild) {
		guild.loadMembers().onSuccess(members ->
				members.forEach(m -> initializeForUser(m.getIdLong()))
		);
	}
}
