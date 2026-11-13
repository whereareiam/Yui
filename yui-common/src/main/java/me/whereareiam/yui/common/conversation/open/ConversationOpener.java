package me.whereareiam.yui.common.conversation.open;

import me.whereareiam.yui.common.conversation.ConversationSetup;
import me.whereareiam.yui.common.conversation.OpenConversation;
import me.whereareiam.yui.type.ConversationType;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Opens conversations of one type. A failure means the type does not work for these members now, and the next type
 * is tried.
 */
public interface ConversationOpener {
	ConversationType type();

	CompletableFuture<OpenConversation> open(String purpose, Set<Long> members, ConversationSetup setup);

	/**
	 * The first message: the members mentioned, when the setup says so, then its message.
	 */
	static String greeting(Set<Long> members, ConversationSetup setup) {
		StringBuilder greeting = new StringBuilder();
		if (setup.mention()) members.forEach(member -> greeting.append("<@").append(member).append("> "));
		if (setup.message() != null) greeting.append(setup.message());

		return greeting.toString().trim();
	}
}
