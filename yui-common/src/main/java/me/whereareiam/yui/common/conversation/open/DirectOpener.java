package me.whereareiam.yui.common.conversation.open;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.type.ConversationType;
import me.whereareiam.yui.common.conversation.ConversationSetup;
import me.whereareiam.yui.common.conversation.OpenConversation;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Opens a direct message. It fails when the member does not accept direct messages from Yui, which only shows when
 * the first message is refused, so a direct conversation always starts with one.
 */
@Component
@RequiredArgsConstructor
public class DirectOpener implements ConversationOpener {
	private final JDA jda;

	@Override
	public ConversationType type() {
		return ConversationType.DIRECT;
	}

	@Override
	public CompletableFuture<OpenConversation> open(String purpose, Set<Long> members, ConversationSetup setup) {
		long member = members.iterator().next();

		return jda.retrieveUserById(member).submit()
				.thenCompose(user -> user.openPrivateChannel().submit())
				.thenCompose(channel -> greet(channel, setup).thenApply(ignored -> conversation(purpose, members, setup, channel)));
	}

	private static CompletableFuture<?> greet(PrivateChannel channel, ConversationSetup setup) {
		String greeting = setup.message() == null ? "" : setup.message();
		if (greeting.isBlank()) return channel.sendTyping().submit();

		return channel.sendMessage(greeting).submit();
	}

	private static OpenConversation conversation(String purpose, Set<Long> members, ConversationSetup setup, PrivateChannel channel) {
		return new OpenConversation(purpose, members, ConversationType.DIRECT, channel, setup, () -> CompletableFuture.completedFuture(null));
	}
}
