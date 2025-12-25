package me.whereareiam.yui.type;

/**
 * Represents the different types of conversations that can be used for communicating with Discord users.
 * <p>
 * This enum serves two primary purposes:
 * <ul>
 *   <li>Identifying the type of an existing conversation via {@link me.whereareiam.yui.conversation.Conversation#getType()}</li>
 *   <li>Specifying preferred conversation types in {@link me.whereareiam.yui.model.ConversationConfig#getPreferredModes()}</li>
 * </ul>
 * <p>
 * When used in configuration, multiple types can be specified in order of preference,
 * allowing automatic fallback if the preferred type is not available.
 *
 * @see me.whereareiam.yui.conversation.Conversation
 * @see me.whereareiam.yui.model.ConversationConfig
 */
public enum ConversationType {
	/**
	 * Private message (DM) conversation with a single user.
	 * <p>
	 * The bot sends direct messages to the user's DM channel.
	 * This type only supports one-to-one conversations and may fail if:
	 * <ul>
	 *   <li>The user has DMs disabled</li>
	 *   <li>The user has blocked the bot</li>
	 *   <li>The user does not share a server with the bot</li>
	 * </ul>
	 */
	PRIVATE_MESSAGE,

	/**
	 * Temporary text channel conversation.
	 * <p>
	 * The bot creates a temporary text channel visible only to the specified users.
	 * This type supports both single-user and multi-user conversations.
	 * The channel is automatically deleted when the conversation is closed.
	 */
	TEMPORARY_CHANNEL
}
