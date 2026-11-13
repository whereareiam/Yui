package me.whereareiam.yui.common.conversation;

import lombok.Getter;
import me.whereareiam.yui.conversation.Conversation;
import me.whereareiam.yui.type.ConversationType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * A conversation Yui has open, with what closing it means for its type: nothing for a direct message, archiving and
 * locking a thread, deleting a channel.
 */
@Getter
public final class OpenConversation implements Conversation {
	private final @NotNull String purpose;
	private final @NotNull Set<Long> members;
	private final @NotNull ConversationType type;
	private final @NotNull MessageChannel channel;
	private final @NotNull ConversationSetup setup;
	private final Supplier<CompletableFuture<Void>> closing;
	private volatile boolean open = true;

	public OpenConversation(
			@NotNull String purpose,
			@NotNull Set<Long> members,
			@NotNull ConversationType type,
			@NotNull MessageChannel channel,
			@NotNull ConversationSetup setup,
			Supplier<CompletableFuture<Void>> closing
	) {
		this.purpose = purpose;
		this.members = Set.copyOf(members);
		this.type = type;
		this.channel = channel;
		this.setup = setup;
		this.closing = closing;
	}

	@Override
	public @NotNull CompletableFuture<Message> send(@NotNull String content) {
		return channel.sendMessage(content).submit();
	}

	/**
	 * Marks it closed and closes its channel the way its type does. Only the first call closes.
	 *
	 * @return completes once the channel is closed
	 */
	synchronized CompletableFuture<Void> shut() {
		if (!open) return CompletableFuture.completedFuture(null);

		open = false;
		return closing.get();
	}
}
