package me.whereareiam.yui.common.service.conversation.type.pm;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.conversation.Conversation;
import me.whereareiam.yui.type.ConversationType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@Getter
@RequiredArgsConstructor
class PrivateMessageConversation implements Conversation {
	private final User user;
	private final PrivateChannel channel;
	private volatile boolean active = true;

	@Override
	public Collection<Long> getParticipants() {
		return Collections.singleton(user.getIdLong());
	}

	@Override
	public ConversationType getType() {
		return ConversationType.PRIVATE_MESSAGE;
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
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Void> close(long delaySeconds) {
		return close();
	}

	@Override
	public boolean isActive() {
		return active;
	}
}
