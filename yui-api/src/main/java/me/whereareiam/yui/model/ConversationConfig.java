package me.whereareiam.yui.model;

import lombok.Builder;
import lombok.Getter;
import me.whereareiam.yui.type.ConversationType;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class ConversationConfig {
	/**
	 * List of conversation types to try in order of preference.
	 * <p>
	 * The service will attempt each type in the list until one succeeds.
	 */
	@Builder.Default
	private final List<ConversationType> preferredModes = Collections.singletonList(ConversationType.TEMPORARY_CHANNEL);

	private final String channelName;
	private final String channelDescription;
	private final String initialMessage;
	private final String privateInitialMessage;
	private final String channelInitialMessage;

	@Builder.Default
	private final boolean mentionUsers = true;

	private final Long closeDelaySeconds;
}
