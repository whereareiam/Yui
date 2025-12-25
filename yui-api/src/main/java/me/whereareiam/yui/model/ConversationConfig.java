package me.whereareiam.yui.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class ConversationConfig {
	/**
	 * List of conversation modes to try in order of preference.
	 * <p>
	 * The service will attempt each mode in the list until one succeeds.
	 */
	@Builder.Default
	private final List<ConversationMode> preferredModes = Collections.singletonList(ConversationMode.TEMPORARY_CHANNEL);

	private final String channelName;
	private final String channelDescription;
	private final String initialMessage;
	private final String privateInitialMessage;
	private final String channelInitialMessage;

	@Builder.Default
	private final boolean mentionUsers = true;

	private final Long closeDelaySeconds;
}
