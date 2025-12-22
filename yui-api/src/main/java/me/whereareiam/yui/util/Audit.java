package me.whereareiam.yui.util;

import lombok.Getter;
import me.whereareiam.yui.service.AuditService;
import me.whereareiam.yui.type.AuditSeverity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Fluent API for building and sending audit log messages.
 * <p>
 * This class provides a builder-style API for constructing audit log entries with
 * placeholders, embeds, and automatic channel routing based on audit types.
 * <p>
 * Usage examples:
 * <pre>{@code
 * // Simple text audit with default INFO severity
 * Audit.log(Constants.AuditTypes.USER_JOIN)
 *     .withText("User <user> joined the server")
 *     .with("user", member.getAsMention())
 *     .send();
 *
 * // Severe audit with ERROR severity
 * Audit.log(Constants.AuditTypes.USER_KICK)
 *     .withSeverity(AuditSeverity.ERROR)
 *     .withEmbed(StyleKit.embeds().error()
 *         .setTitle("User Kicked")
 *         .setDescription("User was kicked for rule violation")
 *         .build())
 *     .send();
 *
 * // Warning severity audit
 * Audit.log("suspicious_activity")
 *     .withSeverity(AuditSeverity.WARNING)
 *     .withText("Suspicious activity detected")
 *     .send();
 * }</pre>
 *
 * @see me.whereareiam.yui.Constants.AuditTypes
 * @see AuditService
 */
@SuppressWarnings("unused")
public class Audit {
	private static AuditService auditService;

	@Component
	static class Initializer {
		@Autowired
		public Initializer(AuditService auditService) {
			Audit.auditService = auditService;
		}
	}

	private final String auditType;
	private final Map<String, Object> placeholders;
	@Getter
	private AuditSeverity severity;
	private MessageEmbed embed;
	private MessageCreateData customMessage;
	private String textTemplate;

	private Audit(String auditType) {
		this.auditType = auditType;
		this.placeholders = new HashMap<>();
		this.severity = AuditSeverity.INFO; // Default severity
	}

	/**
	 * Start building an audit log entry for the specified audit type.
	 *
	 * @param auditType The audit type (e.g., Constants.AuditTypes.USER_JOIN or custom string)
	 * @return A new Audit builder
	 */
	public static Audit log(String auditType) {
		return new Audit(auditType);
	}

	/**
	 * Check if an audit type is configured with a channel.
	 * Useful for avoiding expensive operations when audit logging is disabled.
	 *
	 * @param auditType The audit type to check
	 * @return true if the audit type has a configured channel, false otherwise
	 */
	public static boolean isConfigured(String auditType) {
		return auditService != null && auditService.isConfigured(auditType);
	}

	/**
	 * Add a placeholder value for text template substitution.
	 * <p>
	 * Placeholders in the text template should be enclosed in angle brackets.
	 * Example: "User &lt;user&gt; joined" with placeholder("user", member.getAsMention())
	 *
	 * @param name The placeholder name (without angle brackets)
	 * @param value The value to substitute
	 * @return This Audit builder for chaining
	 */
	public Audit with(String name, Object value) {
		placeholders.put(name, value);
		return this;
	}

	/**
	 * Set the severity level of this audit log.
	 * <p>
	 * Severity determines which StyleKit embed style will be used:
	 * <ul>
	 *   <li>INFO - info embed style (default)</li>
	 *   <li>WARNING - warning embed style</li>
	 *   <li>ERROR - error embed style</li>
	 * </ul>
	 *
	 * @param severity The severity level
	 * @return This Audit builder for chaining
	 */
	public Audit withSeverity(AuditSeverity severity) {
		this.severity = severity;
		return this;
	}

	/**
	 * Set a text template with placeholders.
	 * <p>
	 * Placeholders should be enclosed in angle brackets and will be replaced
	 * with values provided via {@link #with(String, Object)}.
	 * <p>
	 * Example: "User &lt;user&gt; started verification at &lt;timestamp&gt;"
	 *
	 * @param template The text template
	 * @return This Audit builder for chaining
	 */
	public Audit withText(String template) {
		this.textTemplate = template;
		return this;
	}

	/**
	 * Build a custom embed using a consumer.
	 * <p>
	 * The consumer receives an EmbedBuilder that is pre-configured with the
	 * appropriate StyleKit style based on the severity level.
	 * <p>
	 * If no severity is set, INFO style will be used.
	 *
	 * @param embedBuilder Consumer that configures the embed
	 * @return This Audit builder for chaining
	 */
	public Audit withEmbed(Consumer<EmbedBuilder> embedBuilder) {
		EmbedBuilder builder = getStyleKitEmbedForSeverity();
		embedBuilder.accept(builder);
		this.embed = builder.build();
		return this;
	}

	/**
	 * Set a pre-built embed.
	 * <p>
	 * Note: When using this method, StyleKit styling based on severity is NOT applied.
	 * Consider using {@link #withEmbed(Consumer)} for automatic style application.
	 *
	 * @param embed The embed to send
	 * @return This Audit builder for chaining
	 */
	public Audit withEmbed(MessageEmbed embed) {
		this.embed = embed;
		return this;
	}

	/**
	 * Set custom message data.
	 *
	 * @param message The message data to send
	 * @return This Audit builder for chaining
	 */
	public Audit withMessage(MessageCreateData message) {
		this.customMessage = message;
		return this;
	}

	/**
	 * Send the audit log entry to the configured channel.
	 * <p>
	 * If the audit type is not configured, this returns a completed future immediately
	 * without sending anything.
	 * <p>
	 * Priority order:
	 * <ol>
	 *   <li>Custom message data (if set via {@link #withMessage(MessageCreateData)})</li>
	 *   <li>Embed (if set via {@link #withEmbed(Consumer)} or {@link #withEmbed(MessageEmbed)})</li>
	 *   <li>Text template with placeholders (if set via {@link #withText(String)})</li>
	 * </ol>
	 *
	 * @return CompletableFuture that completes when the message is sent
	 */
	public CompletableFuture<Void> send() {
		if (auditService == null)
			return CompletableFuture.completedFuture(null);

		if (customMessage != null)
			return auditService.audit(auditType, customMessage);

		if (embed != null)
			return auditService.audit(auditType, embed);

		if (textTemplate != null) {
			String message = replacePlaceholders(textTemplate);
			return auditService.audit(auditType, message);
		}

		return CompletableFuture.completedFuture(null);
	}

	private String replacePlaceholders(String template) {
		String result = template;
		for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
			result = result.replace("<" + entry.getKey() + ">",
					String.valueOf(entry.getValue()));
		}

		return result;
	}

	private EmbedBuilder getStyleKitEmbedForSeverity() {
		if (!isStyleKitAvailable()) {
			return new EmbedBuilder();
		}

		return switch (severity) {
			case WARNING -> me.whereareiam.yui.util.style.StyleKit.embeds().warning();
			case ERROR -> me.whereareiam.yui.util.style.StyleKit.embeds().error();
			default -> me.whereareiam.yui.util.style.StyleKit.embeds().info();
		};
	}

	private boolean isStyleKitAvailable() {
		try {
			me.whereareiam.yui.util.style.StyleKit.embeds();
			return true;
		} catch (IllegalStateException e) {
			return false;
		}
	}
}
