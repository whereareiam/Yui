package me.whereareiam.yui.common.conversation;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * What happens to open conversations outside Yui: a message starts the idle time again, a deleted channel ends its
 * conversation, and a member who leaves the server closes theirs.
 */
@Component
@RequiredArgsConstructor
public class ConversationActivity extends ListenerAdapter {
	private final ConversationLedger ledger;
	private final ConversationClosing closing;

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		ledger.byChannel(event.getChannel().getIdLong()).ifPresent(closing::touched);
	}

	@Override
	public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
		ledger.byChannel(event.getChannel().getIdLong()).ifPresent(closing::lost);
	}

	@Override
	public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
		ledger.of(event.getUser().getIdLong()).forEach(closing::close);
	}
}
