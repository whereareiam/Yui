package me.whereareiam.yui.common.service.conversation.type.pm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.conversation.Conversation;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Creates private-message based conversations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PrivateMessageConversationCreator {
	private final JDA jda;

	public CompletableFuture<Conversation> create(long userId, String initialMessage) {
		CompletableFuture<Conversation> future = new CompletableFuture<>();

		jda.retrieveUserById(userId).queue(
				user -> user.openPrivateChannel().queue(
						channel -> sendInitial(user, channel, initialMessage, future),
						ex -> {
							log.debug("Failed to open PM for user {}: {}", userId, ex.getMessage());
							future.completeExceptionally(ex);
						}
				),
				future::completeExceptionally
		);

		return future;
	}

	private void sendInitial(User user, PrivateChannel channel, String initialMessage, CompletableFuture<Conversation> future) {
		// If no initial message, just complete with the conversation
		if (initialMessage == null || initialMessage.isBlank()) {
			future.complete(new PrivateMessageConversation(user, channel));
			return;
		}

		channel.sendMessage(initialMessage).queue(
				_ -> future.complete(new PrivateMessageConversation(user, channel)),
				ex -> {
					log.debug("Cannot send PM to user {}: {}", user.getIdLong(), ex.getMessage());
					future.completeExceptionally(ex);
				}
		);
	}
}
