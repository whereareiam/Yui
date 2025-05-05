package me.whereareiam.yui.api.input;

import me.whereareiam.yui.api.model.channel.ChannelDecoration;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface TemporaryChannelService {
	CompletableFuture<TextChannel> create(Collection<Long> userIds);

	CompletableFuture<TextChannel> create(Collection<Long> userIds, ChannelDecoration decoration);

	CompletableFuture<Void> close(TextChannel channel);

	CompletableFuture<Void> close(TextChannel channel, long delay);

	Optional<TextChannel> findByUser(long userId);
}
