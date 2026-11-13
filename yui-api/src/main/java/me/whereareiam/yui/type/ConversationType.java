package me.whereareiam.yui.type;

/**
 * Where a conversation happens. {@code conversations.yml} says which of them this server allows and in which order
 * they are tried; a conversation with more than one member is never direct.
 */
public enum ConversationType {
	/**
	 * A direct message. Fails when the member does not accept direct messages from Yui.
	 */
	DIRECT,

	/**
	 * A private thread under a channel. Discord archives it when it goes quiet and keeps it readable; staff see it
	 * through Manage Threads on that channel, or because Yui adds them.
	 */
	THREAD,

	/**
	 * A temporary text channel in one of the configured categories, with permissions of its own; Yui deletes it when
	 * the conversation closes.
	 */
	CHANNEL
}
