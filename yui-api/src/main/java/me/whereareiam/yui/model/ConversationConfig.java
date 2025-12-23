package me.whereareiam.yui.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConversationConfig {
	@Builder.Default
	private final boolean preferPrivateMessage = false;

	@Builder.Default
	private final boolean allowTemporaryChannel = true;

	private final String channelName;
	private final String channelDescription;
	private final String initialMessage;
	private final String privateInitialMessage;
	private final String channelInitialMessage;

	@Builder.Default
	private final boolean mentionUsers = true;

	private final Long closeDelaySeconds;
}
