package me.whereareiam.yui.api.conversation;

import me.whereareiam.yui.api.type.ConversationType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Yui talking privately with members for one purpose, in a direct message, a private thread or a temporary
 * channel. {@link ConversationService} opens and closes it; it closes on its own when {@code conversations.yml} or
 * the options it was opened with say.
 */
public interface Conversation {
	/**
	 * @return what the conversation is for, such as {@code verification}
	 */
	@NotNull String getPurpose();

	/**
	 * @return the members in it, by user id
	 */
	@NotNull Set<Long> getMembers();

	@NotNull ConversationType getType();

	/**
	 * @return where its messages are
	 */
	@NotNull MessageChannel getChannel();

	@NotNull CompletableFuture<Message> send(@NotNull String content);

	/**
	 * @return false once it is closed
	 */
	boolean isOpen();
}
