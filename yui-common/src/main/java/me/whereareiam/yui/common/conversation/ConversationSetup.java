package me.whereareiam.yui.common.conversation;

import me.whereareiam.yui.api.conversation.ConversationOptions;
import me.whereareiam.yui.api.model.config.conversation.Conversations;
import me.whereareiam.yui.api.type.ConversationType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * How one conversation is opened and behaves: the {@code defaults} of {@code conversations.yml} with what its
 * options change, the types this server does not allow left out, and no direct message for several members.
 *
 * @param via    the types to try, in order
 * @param parent the channel a thread is made under
 * @param name   the thread's or channel's name
 * @param message the first message
 */
public record ConversationSetup(
		List<ConversationType> via,
		@Nullable String parent,
		boolean mention,
		List<String> staff,
		Conversations.Close close,
		Conversations.Limit limit,
		Conversations.Transcript transcript,
		@Nullable String name,
		@Nullable String message
) {
	static ConversationSetup of(Conversations config, ConversationOptions options, int members) {
		Conversations.Defaults defaults = config.getDefaults();
		List<ConversationType> via = Objects.requireNonNullElse(options.getVia(), defaults.getVia()).stream()
				.filter(type -> allowed(config.getTypes(), type))
				.filter(type -> type != ConversationType.DIRECT || members == 1)
				.distinct()
				.toList();

		return new ConversationSetup(
				via,
				options.getParent() != null ? options.getParent() : config.getTypes().getThread().getParent(),
				Objects.requireNonNullElse(options.getMention(), defaults.isMention()),
				Objects.requireNonNullElse(options.getStaff(), defaults.getStaff()),
				Objects.requireNonNullElse(options.getClose(), defaults.getClose()),
				Objects.requireNonNullElse(options.getLimit(), defaults.getLimit()),
				Objects.requireNonNullElse(options.getTranscript(), defaults.getTranscript()),
				options.getName(),
				options.getMessage()
		);
	}

	private static boolean allowed(Conversations.Types types, ConversationType type) {
		return switch (type) {
			case DIRECT -> types.getDirect().isEnabled();
			case THREAD -> types.getThread().isEnabled();
			case CHANNEL -> types.getChannel().isEnabled();
		};
	}
}
