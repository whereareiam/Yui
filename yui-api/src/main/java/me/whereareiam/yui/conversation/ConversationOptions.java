package me.whereareiam.yui.conversation;

import lombok.Builder;
import lombok.Getter;
import me.whereareiam.yui.model.config.conversation.Conversations;
import me.whereareiam.yui.type.ConversationType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * What one call to {@link ConversationService#open} changes for its own use case.
 * Everything left out comes from the {@code defaults} of {@code conversations.yml}; a close, limit or transcript
 * given here replaces the default one as a whole.
 *
 * <pre>{@code
 * conversations.open("verification", List.of(memberId), ConversationOptions.builder()
 *         .via(List.of(ConversationType.THREAD, ConversationType.DIRECT))
 *         .parent(verifyChannelId)
 *         .name("verification-" + member.getEffectiveName())
 *         .message(welcome)
 *         .build());
 * }</pre>
 */
@Getter
@Builder
public class ConversationOptions {
	private final @Nullable List<ConversationType> via;
	private final @Nullable String parent;
	private final @Nullable Boolean mention;
	private final @Nullable List<String> staff;
	private final Conversations.@Nullable Close close;
	private final Conversations.@Nullable Limit limit;
	private final Conversations.@Nullable Transcript transcript;

	/**
	 * The thread's or channel's name, a text in the members' language.
	 */
	private final @Nullable String name;

	/**
	 * The first message, a text in the members' language.
	 */
	private final @Nullable String message;

	/**
	 * @return options that change nothing
	 */
	public static ConversationOptions none() {
		return builder().build();
	}
}
