package me.whereareiam.yui.service;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service for auditing events to configured Discord channels.
 * <p>
 * This service allows logging of events (user joins, moderation actions, verification steps, etc.)
 * to specific Discord channels based on configured audit types.
 * <p>
 * Each audit type can be mapped to a channel ID in the configuration. If an audit type
 * is not configured, the audit message is silently ignored.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * auditService.audit(Constants.AuditTypes.USER_JOIN, "User joined the server");
 * auditService.audit("verification_start", embedBuilder.build());
 * }</pre>
 *
 * @see me.whereareiam.yui.Constants.AuditTypes
 */
@SuppressWarnings("unused")
public interface AuditService {
	/**
	 * Send an audit message to the configured channel for this audit type.
	 *
	 * @param auditType The type of audit (e.g., Constants.AuditTypes.USER_JOIN)
	 * @param message The message content to send
	 * @return CompletableFuture that completes when the message is sent, or immediately if not configured
	 */
	CompletableFuture<Void> audit(String auditType, String message);

	/**
	 * Send an audit message with an embed to the configured channel.
	 *
	 * @param auditType The type of audit
	 * @param embed The embed to send
	 * @return CompletableFuture that completes when the message is sent, or immediately if not configured
	 */
	CompletableFuture<Void> audit(String auditType, MessageEmbed embed);

	/**
	 * Send an audit message with custom message data to the configured channel.
	 *
	 * @param auditType The type of audit (e.g., Constants.AuditTypes.USER_JOIN).
	 *                  This determines the category of the event being logged.
	 * @param message   The message data to send, which can include text, embeds, or other rich content.
	 * @return CompletableFuture that completes when the message is sent successfully, or immediately if the audit type is not configured.
	 */
	CompletableFuture<Void> audit(String auditType, MessageCreateData message);

	/**
	 * Send an audit message with custom message data to the configured channel, considering locale-specific configurations.
	 * <p>
	 * This method attempts to send the provided message to a channel configured for the given audit type and locale.
	 * If a locale-specific channel is not configured, it falls back to the default channel for the audit type.
	 *
	 * @param auditType The type of audit (e.g., Constants.AuditTypes.USER_JOIN).
	 *                  This determines the category of the event being logged.
	 * @param message   The message data to send, which can include text, embeds, or other rich content.
	 * @param locale    The Discord locale to consider for sending the message to a locale-specific channel.
	 *                  If no locale-specific channel is configured, the default channel is used.
	 * @return CompletableFuture that completes when the message is sent successfully, or immediately if the audit type is not configured.
	 */
	CompletableFuture<Void> audit(String auditType, MessageCreateData message, DiscordLocale locale);

	/**
	 * Check if an audit type is configured with a channel.
	 *
	 * @param auditType The audit type to check
	 * @return true if the audit type has a configured channel, false otherwise
	 */
	boolean isConfigured(String auditType);

	/**
	 * Get the channel IDs configured for an audit type.
	 *
	 * @param auditType The audit type
	 * @return Optional containing the channel IDs if configured, empty otherwise
	 */
	Optional<List<String>> getChannelIds(String auditType, DiscordLocale locale);

	/**
	 * Legacy helper for single-channel configurations. Returns the first configured channel ID if present.
	 */
	default Optional<String> getChannelId(String auditType) {
		return getChannelIds(auditType, null)
				.filter(list -> !list.isEmpty())
				.map(List::getFirst);
	}

	/**
	 * Locales configured via channel prefixes for this audit type (e.g., "ru:channel").
	 *
	 * @param auditType The audit type
	 * @return Locales derived from prefixes (empty if none)
	 */
	default List<DiscordLocale> getConfiguredLocales(String auditType) {
		return List.of();
	}
}
