package me.whereareiam.yui.util.audit;

import me.whereareiam.yui.service.AuditService;
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
 * // Simple text audit
 * Audit.log(Constants.AuditTypes.USER_JOIN)
 *     .withText("User <user> joined the server")
 *     .with("user", member.getAsMention())
 *     .send();
 *
 * // Rich embed audit
 * Audit.log(Constants.AuditTypes.MODERATION)
 *     .withEmbed(embed -> embed
 *         .setTitle("User Kicked")
 *         .setDescription("User was kicked for rule violation")
 *         .addField("User", member.getAsMention(), true)
 *         .addField("Moderator", moderator.getAsMention(), true)
 *         .setColor(Color.RED)
 *         .setTimestamp(Instant.now()))
 *     .send();
 *
 * // Check if configured before building
 * if (Audit.isConfigured("verification_start")) {
 *     Audit.log("verification_start")
 *         .withEmbed(embed -> embed.setTitle("Verification Started"))
 *         .send();
 * }
 *
 * // Plugin usage with custom audit type
 * Audit.log("my_plugin_custom_event")
 *     .withText("Custom event occurred")
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
	private MessageEmbed embed;
	private MessageCreateData customMessage;
	private String textTemplate;

	private Audit(String auditType) {
		this.auditType = auditType;
		this.placeholders = new HashMap<>();
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
	 * The consumer receives an EmbedBuilder to configure.
	 *
	 * @param embedBuilder Consumer that configures the embed
	 * @return This Audit builder for chaining
	 */
	public Audit withEmbed(Consumer<EmbedBuilder> embedBuilder) {
		EmbedBuilder builder = new EmbedBuilder();
		embedBuilder.accept(builder);
		this.embed = builder.build();
		return this;
	}

	/**
	 * Set a pre-built embed.
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
}
