package me.whereareiam.yui.common.service.conversation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.model.conversation.Conversation;
import me.whereareiam.yui.model.conversation.ConversationType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Getter
@RequiredArgsConstructor
class TemporaryChannelConversation implements Conversation {
	private final Set<Long> participants;
	private final TextChannel channel;
	private final DefaultConversationService conversationService;
	private volatile boolean active = true;

	@Override
	public Collection<Long> getParticipants() {
		return new HashSet<>(participants);
	}

	@Override
	public ConversationType getType() {
		return ConversationType.TEMPORARY_CHANNEL;
	}

	@Override
	public MessageChannelUnion getChannel() {
		return (MessageChannelUnion) channel;
	}

	@Override
	public CompletableFuture<Message> sendMessage(String content) {
		CompletableFuture<Message> future = new CompletableFuture<>();
		channel.sendMessage(content).queue(future::complete, future::completeExceptionally);
		return future;
	}

	@Override
	public CompletableFuture<Void> close() {
		active = false;
		return conversationService.closeChannel(channel, 0);
	}

	@Override
	public CompletableFuture<Void> close(long delaySeconds) {
		active = false;
		return conversationService.closeChannel(channel, delaySeconds);
	}

	@Override
	public boolean isActive() {
		return active;
	}
}
