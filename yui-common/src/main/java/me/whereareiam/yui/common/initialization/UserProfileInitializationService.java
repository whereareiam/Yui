package me.whereareiam.yui.common.initialization;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.fluctlight.FluctlightService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserProfileInitializationService {
	private final FluctlightService fluctlightService;
	private final JDA jda;

	@Order(Integer.MIN_VALUE)
	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		jda.getGuilds().forEach(this::initializeForGuild);
	}

	public void initializeForUser(long userId) {
		fluctlightService.getOrCreate(userId);
	}

	public void initializeForGuild(Guild guild) {
		guild.loadMembers().onSuccess(members ->
				members.forEach(m -> initializeForUser(m.getIdLong()))
		);
	}
}
