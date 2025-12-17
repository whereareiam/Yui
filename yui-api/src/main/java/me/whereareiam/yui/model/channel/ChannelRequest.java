package me.whereareiam.yui.model.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Getter
@AllArgsConstructor
public class ChannelRequest {
	private final Collection<Long> userIds;
	private final ChannelDecoration decoration;
	private final CompletableFuture<TextChannel> future;
}
