package me.whereareiam.yui.model.conversation;

import me.whereareiam.yui.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface Conversation {
	Collection<Long> getParticipants();

	ConversationType getType();

	MessageChannelUnion getChannel();

	CompletableFuture<Message> sendMessage(String content);

	CompletableFuture<Void> close();

	CompletableFuture<Void> close(long delaySeconds);

	boolean isActive();
}
