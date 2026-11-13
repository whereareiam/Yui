package me.whereareiam.yui.api.conversation;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Opens conversations: Yui talking privately with members for a purpose. {@code conversations.yml} says which types
 * this server allows and how a conversation behaves by default; each call may change that for its own use case.
 *
 * <pre>{@code
 * conversations.open("onboarding", List.of(memberId))
 *         .thenAccept(conversation -> conversation.send("Welcome!"));
 * }</pre>
 *
 * <p>The types are tried in order until one works; a conversation with more than one member is never direct. A
 * member who already has an open conversation of that purpose gets it back, and one still in the purpose's cooldown
 * gets none.</p>
 */
public interface ConversationService {
	/**
	 * Opens a conversation the way {@code conversations.yml} says.
	 *
	 * @param purpose what it is for, such as {@code onboarding}
	 * @param members the members in it, by user id
	 * @return the conversation, or failed with {@link me.whereareiam.yui.api.exception.ConversationRefusedException} when no type works or a limit
	 * keeps it closed
	 */
	default @NotNull CompletableFuture<Conversation> open(@NotNull String purpose, @NotNull Collection<Long> members) {
		return open(purpose, members, ConversationOptions.none());
	}

	/**
	 * Opens a conversation, with what the options change over {@code conversations.yml}.
	 *
	 * @param purpose what it is for
	 * @param members the members in it, by user id
	 * @param options what this use case changes
	 * @return the conversation, or failed with {@link me.whereareiam.yui.api.exception.ConversationRefusedException}
	 */
	@NotNull CompletableFuture<Conversation> open(
			@NotNull String purpose,
			@NotNull Collection<Long> members,
			@NotNull ConversationOptions options
	);

	/**
	 * @param member  a user id
	 * @param purpose what the conversation is for
	 * @return the member's open conversation of that purpose
	 */
	@NotNull Optional<Conversation> find(long member, @NotNull String purpose);

	/**
	 * Closes a conversation now: a thread is archived and locked, a channel deleted. Its transcript goes where the
	 * options say.
	 *
	 * @param conversation the conversation
	 * @return completes once it is closed
	 */
	@NotNull CompletableFuture<Void> close(@NotNull Conversation conversation);
}
