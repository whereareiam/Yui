package me.whereareiam.yui.common.listener;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.common.service.conversation.DefaultConversationService;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TextChannelDeleteListener extends ListenerAdapter {
	private final DefaultConversationService conversationService;

	@Override
	public void onChannelDelete(ChannelDeleteEvent event) {
		if (event.getChannel() instanceof TextChannel tc)
			conversationService.handleChannelDeletion(tc.getIdLong());
	}
}
