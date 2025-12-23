package me.whereareiam.yui.conversation;

import me.whereareiam.yui.type.ConversationType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public interface Conversation {
	Collection<Long> getParticipants();

	ConversationType getType();

	MessageChannelUnion getChannel();

	CompletableFuture<Message> sendMessage(String content);

	CompletableFuture<Void> close();

	CompletableFuture<Void> close(long delaySeconds);

	boolean isActive();
}
