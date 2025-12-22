package me.whereareiam.yui.service;

import me.whereareiam.yui.model.conversation.Conversation;
import me.whereareiam.yui.model.conversation.ConversationConfig;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing conversations with Discord users through multiple channels.
 * <p>
 * This service provides a flexible system to communicate with users via either
 * private messages (DMs) or temporary text channels. It automatically handles
 * fallback scenarios when private messages are not available.
 * <p>
 * <b>Key Features:</b>
 * <ul>
 *   <li>Automatic PM/channel fallback - tries PM first if preferred, falls back to temp channel</li>
 *   <li>Multi-user conversations - supports temporary channels with multiple participants</li>
 *   <li>Context-based isolation - multiple conversations per user for different purposes</li>
 *   <li>Automatic lifecycle management - handles channel cleanup and timeout</li>
 *   <li>Thread-safe operations - all operations are concurrent-safe</li>
 * </ul>
 * <p>
 * <b>Conversation Contexts:</b>
 * <p>
 * Each conversation is associated with a context string that identifies its purpose.
 * This allows multiple simultaneous conversations with the same user for different
 * purposes (e.g., "verification", "support", "moderation").
 * <p>
 * If a conversation with the same user and context already exists and is active,
 * the existing conversation will be returned instead of creating a new one.
 * <p>
 * <b>Usage Example:</b>
 * <pre>{@code
 * // Create a verification conversation
 * conversationService.create(userId, "verification")
 *     .thenAccept(conversation -> {
 *         conversation.sendMessage("Hello!");
 *     });
 *
 * // Create a support conversation (can coexist with verification)
 * conversationService.create(userId, "support")
 *     .thenAccept(conversation -> {
 *         conversation.sendMessage("How can we help?");
 *     });
 *
 * // Create with custom configuration
 * ConversationConfig config = ConversationConfig.builder()
 *     .preferPrivateMessage(true)
 *     .allowTemporaryChannel(true)
 *     .initialMessage("Welcome to verification!")
 *     .timeoutSeconds(300)
 *     .closeDelaySeconds(10)
 *     .build();
 *
 * conversationService.create(userId, "verification", config)
 *     .thenAccept(conversation -> {
 *         // Use the conversation
 *     });
 *
 * // Create a multi-user conversation
 * conversationService.create(Arrays.asList(user1Id, user2Id), "team-discussion", config)
 *     .thenAccept(conversation -> {
 *         // Temporary channel will be created with all users
 *     });
 * }</pre>
 *
 * @see Conversation
 * @see ConversationConfig
 */
@SuppressWarnings("unused")
public interface ConversationService {
	/**
	 * Creates a conversation with a single user using default configuration.
	 * <p>
	 * This method will attempt to create a temporary channel conversation by default.
	 *
	 * @param userId The Discord user ID to create a conversation with
	 * @param context The context identifier for this conversation (e.g., "verification", "support")
	 * @return A CompletableFuture containing the created Conversation
	 * @see #create(long, String, ConversationConfig)
	 */
	CompletableFuture<Conversation> create(long userId, String context);

	/**
	 * Creates a conversation with a single user using custom configuration.
	 * <p>
	 * The service will attempt to create a conversation according to the configuration:
	 * <ol>
	 *   <li>If {@code preferPrivateMessage} is true, tries to create a PM first</li>
	 *   <li>If PM fails or is not preferred, and {@code allowTemporaryChannel} is true,
	 *       creates a temporary channel</li>
	 *   <li>If PM is not preferred but temp channels are disabled, tries PM anyway</li>
	 *   <li>If all options fail, returns a failed CompletableFuture</li>
	 * </ol>
	 * <p>
	 * If a conversation already exists for the user with the same context and is
	 * still active, returns the existing conversation instead of creating a new one.
	 *
	 * @param userId The Discord user ID to create a conversation with
	 * @param context The context identifier for this conversation (e.g., "verification", "support")
	 * @param config The configuration for the conversation (behavior, messages, timeouts)
	 * @return A CompletableFuture containing the created or existing Conversation
	 * @throws IllegalArgumentException if userId is invalid or context is null/empty
	 */
	CompletableFuture<Conversation> create(long userId, String context, ConversationConfig config);

	/**
	 * Creates a conversation with multiple users using default configuration.
	 * <p>
	 * Since private messages only support one-to-one communication, this method
	 * will always create a temporary text channel with all specified users.
	 *
	 * @param userIds Collection of Discord user IDs to include in the conversation
	 * @param context The context identifier for this conversation
	 * @return A CompletableFuture containing the created Conversation
	 * @see #create(Collection, String, ConversationConfig)
	 */
	CompletableFuture<Conversation> create(Collection<Long> userIds, String context);

	/**
	 * Creates a conversation with multiple users using custom configuration.
	 * <p>
	 * For single-user collections, behaves like {@link #create(long, String, ConversationConfig)}.
	 * For multi-user collections, creates a temporary text channel with all users,
	 * ignoring the {@code preferPrivateMessage} setting.
	 * <p>
	 * If a conversation already exists for any of the users with the same context
	 * and is still active, returns the existing conversation.
	 *
	 * @param userIds Collection of Discord user IDs to include in the conversation
	 * @param context The context identifier for this conversation
	 * @param config The configuration for the conversation
	 * @return A CompletableFuture containing the created or existing Conversation
	 * @throws IllegalArgumentException if userIds is empty or context is null/empty
	 * @throws IllegalStateException if conversation cannot be created with given constraints
	 */
	CompletableFuture<Conversation> create(Collection<Long> userIds, String context, ConversationConfig config);

	/**
	 * Finds an active conversation for the specified user and context.
	 * <p>
	 * Returns the conversation if:
	 * <ul>
	 *   <li>A conversation exists for the user with the specified context</li>
	 *   <li>The conversation is still active ({@link Conversation#isActive()} returns true)</li>
	 * </ul>
	 * If the conversation exists but is no longer active, it will be cleaned up
	 * and an empty Optional will be returned.
	 *
	 * @param userId The Discord user ID to search for
	 * @param context The context identifier of the conversation
	 * @return An Optional containing the active Conversation, or empty if none exists
	 */
	Optional<Conversation> findByUser(long userId, String context);

	/**
	 * Closes a conversation immediately.
	 * <p>
	 * For private message conversations, this marks the conversation as inactive.
	 * For temporary channel conversations, this deletes the channel immediately.
	 * <p>
	 * The conversation is removed from the active conversations registry.
	 *
	 * @param conversation The conversation to close
	 * @return A CompletableFuture that completes when the conversation is closed
	 * @see #close(Conversation, long)
	 */
	CompletableFuture<Void> close(Conversation conversation);

	/**
	 * Closes a conversation after a specified delay.
	 * <p>
	 * For temporary channel conversations, sends a warning message to the channel
	 * indicating when it will be closed, then closes it after the delay.
	 * For private message conversations, the delay is ignored and it closes immediately.
	 * <p>
	 * If delaySeconds is 0 or negative, behaves like {@link #close(Conversation)}.
	 *
	 * @param conversation The conversation to close
	 * @param delaySeconds The delay in seconds before closing
	 * @return A CompletableFuture that completes when the conversation is closed
	 */
	CompletableFuture<Void> close(Conversation conversation, long delaySeconds);
}
