package me.whereareiam.yui.util;

import lombok.Getter;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.service.AuditService;
import me.whereareiam.yui.type.AuditSeverity;
import me.whereareiam.yui.util.style.StyleKit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

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
	private static ObjectProvider<Settings> settingsProvider;

	@Component
	static class Initializer {
		@Autowired
		public Initializer(AuditService auditService, ObjectProvider<Settings> settingsProvider) {
			Audit.auditService = auditService;
			Audit.settingsProvider = settingsProvider;
		}
	}

	private final String auditType;
	private final Map<String, Object> placeholders;
	@Getter
	private AuditSeverity severity;
	private MessageEmbed embed;
	private MessageCreateData customMessage;
	private String textTemplate;
	private DiscordLocale locale;
	private Function<DiscordLocale, MessageCreateData> localizedMessageFactory;
	private Function<DiscordLocale, MessageEmbed> localizedEmbedFactory;
	private Function<DiscordLocale, String> localizedTextFactory;

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
	 * @param name  The placeholder name (without angle brackets)
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
	 * Set a preferred locale for this audit log entry.
	 * <p>
	 * The specified locale is used to determine the target channel for the audit log.
	 * If a channel is configured for the given locale, the message will be routed there.
	 * Otherwise, the default channel for the audit type will be used.
	 *
	 * @param locale The Discord locale to use for channel routing and message translation.
	 * @return This Audit builder for chaining.
	 */
	public Audit withLocale(DiscordLocale locale) {
		this.locale = locale;
		return this;
	}

	/**
	 * Provide a factory for creating locale-aware messages.
	 * <p>
	 * This method allows you to define a function that generates custom message data
	 * for each locale. The generated messages will be sent to the corresponding
	 * locale-specific channels, if configured, or to the default channel otherwise.
	 *
	 * @param factory A function that takes a DiscordLocale and returns MessageCreateData.
	 * @return This Audit builder for chaining.
	 */
	public Audit withLocalizedMessage(Function<DiscordLocale, MessageCreateData> factory) {
		this.localizedMessageFactory = factory;
		return this;
	}

	/**
	 * Provide a factory for creating locale-aware embeds.
	 * <p>
	 * This method allows you to define a function that generates custom embeds
	 * for each locale. The generated embeds will be sent to the corresponding
	 * locale-specific channels, if configured, or to the default channel otherwise.
	 *
	 * @param factory A function that takes a DiscordLocale and returns a MessageEmbed.
	 * @return This Audit builder for chaining.
	 */
	public Audit withLocalizedEmbed(Function<DiscordLocale, MessageEmbed> factory) {
		this.localizedEmbedFactory = factory;
		return this;
	}

	/**
	 * Provide a factory for creating locale-aware text messages.
	 * <p>
	 * This method allows you to define a function that generates custom text messages
	 * for each locale. The generated text messages will be sent to the corresponding
	 * locale-specific channels, if configured, or to the default channel otherwise.
	 *
	 * @param factory A function that takes a DiscordLocale and returns a String.
	 * @return This Audit builder for chaining.
	 */
	public Audit withLocalizedText(Function<DiscordLocale, String> factory) {
		this.localizedTextFactory = factory;
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
		if (auditService == null) return CompletableFuture.completedFuture(null);

		if (localizedMessageFactory != null) return sendLocalizedMessages();

		if (localizedEmbedFactory != null) return sendLocalizedEmbeds();

		if (localizedTextFactory != null) return sendLocalizedTexts();

		if (customMessage != null) return auditService.audit(auditType, customMessage, locale);

		if (embed != null) return auditService.audit(auditType, MessageCreateData.fromEmbeds(embed), locale);

		if (textTemplate != null) {
			String message = replacePlaceholders(textTemplate);
			return auditService.audit(auditType, MessageCreateData.fromContent(message), locale);
		}

		return CompletableFuture.completedFuture(null);
	}

	private String replacePlaceholders(String template) {
		String result = template;
		for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
			result = result.replace("<" + entry.getKey() + ">", String.valueOf(entry.getValue()));
		}

		return result;
	}

	private EmbedBuilder getStyleKitEmbedForSeverity() {
		return switch (severity) {
			case WARNING -> StyleKit.embeds().warning();
			case ERROR -> StyleKit.embeds().error();
			default -> StyleKit.embeds().info();
		};
	}

	private CompletableFuture<Void> sendLocalizedMessages() {
		var locales = resolveTargetLocales();
		return CompletableFuture.allOf(locales.stream().map(loc -> auditService.audit(auditType, localizedMessageFactory.apply(loc), loc)).toArray(CompletableFuture[]::new));
	}

	private CompletableFuture<Void> sendLocalizedEmbeds() {
		var locales = resolveTargetLocales();
		return CompletableFuture.allOf(locales
				.stream()
				.map(loc -> auditService.audit(auditType, MessageCreateData.fromEmbeds(localizedEmbedFactory.apply(loc)), loc))
				.toArray(CompletableFuture[]::new));
	}

	private CompletableFuture<Void> sendLocalizedTexts() {
		var locales = resolveTargetLocales();
		return CompletableFuture.allOf(locales
				.stream()
				.map(loc -> auditService.audit(auditType, MessageCreateData.fromContent(localizedTextFactory.apply(loc)), loc))
				.toArray(CompletableFuture[]::new));
	}

	private List<DiscordLocale> resolveTargetLocales() {
		var locales = new LinkedHashSet<DiscordLocale>();
		if (settingsProvider != null && settingsProvider.getIfAvailable() != null)
			locales.add(settingsProvider.getObject().getLocale());

		locales.addAll(auditService.getConfiguredLocales(auditType));
		if (locale != null) locales.add(locale);

		return List.copyOf(locales);
	}
}
