package me.whereareiam.yue.common.listener;

import lombok.AllArgsConstructor;
import me.whereareiam.yue.common.service.DefaultTemporaryChannelService;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TextChannelDeleteListener extends ListenerAdapter {
	private final DefaultTemporaryChannelService temporaryChannelService;

	@Override
	public void onChannelDelete(ChannelDeleteEvent event) {
		if (event.getChannel() instanceof TextChannel tc)
			temporaryChannelService.handleChannelDeletion(tc.getIdLong());
	}
}
