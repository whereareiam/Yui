package me.whereareiam.yui.util;

import lombok.Getter;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.service.AuditService;
import me.whereareiam.yui.type.audit.AuditCategory;
import me.whereareiam.yui.type.audit.AuditSeverity;
import me.whereareiam.yui.util.style.StyleKit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Builds an entry of the audit trail and posts it where {@code audit.yml} says. The writer files each entry under a
 * category; the entry is rendered in Yui's default locale unless {@link #withLocale(DiscordLocale)} names another.
 *
 * <pre>{@code
 * Audit.log(AuditCategory.MEMBERS, Constants.AuditEntries.MEMBER_JOIN)
 *     .withText("User <user> joined the server")
 *     .with("user", member.getAsMention())
 *     .send();
 *
 * Audit.log(AuditCategory.MODERATION, Constants.AuditEntries.MEMBER_KICK)
 *     .withSeverity(AuditSeverity.ERROR)
 *     .withEmbed(embed -> embed.setTitle("User Kicked"))
 *     .send();
 * }</pre>
 *
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

	private final AuditCategory category;
	private final String entry;
	private final Map<String, Object> placeholders = new HashMap<>();
	@Getter
	private AuditSeverity severity = AuditSeverity.INFO;
	private DiscordLocale locale;
	private Function<DiscordLocale, MessageCreateData> message;

	private Audit(AuditCategory category, String entry) {
		this.category = category;
		this.entry = entry;
	}

	/**
	 * Starts an entry.
	 *
	 * @param category what the entry is about
	 * @param entry    its name, such as {@code member.join}; {@code audit.yml} may send it elsewhere by that name
	 * @return a new builder
	 */
	public static Audit log(AuditCategory category, String entry) {
		return new Audit(category, entry);
	}

	/**
	 * Whether an entry is posted at all, to skip building one that is not.
	 *
	 * @param category what the entry is about
	 * @param entry    its name
	 * @return true when it has a channel
	 */
	public static boolean isPosted(AuditCategory category, String entry) {
		return auditService != null && auditService.isRecorded(category, entry);
	}

	/**
	 * Adds a value for a placeholder of the text ({@code <user>}).
	 *
	 * @param name  the placeholder's name, without angle brackets
	 * @param value its value
	 * @return this builder
	 */
	public Audit with(String name, Object value) {
		placeholders.put(name, value);
		return this;
	}

	/**
	 * Sets how severe the entry is, which picks the embed kind {@link #withEmbed(Consumer)} starts from: info,
	 * warning or error.
	 *
	 * @param severity the severity
	 * @return this builder
	 */
	public Audit withSeverity(AuditSeverity severity) {
		this.severity = severity;
		return this;
	}

	/**
	 * Renders the entry in another locale than Yui's default.
	 *
	 * @param locale the locale
	 * @return this builder
	 */
	public Audit withLocale(DiscordLocale locale) {
		this.locale = locale;
		return this;
	}

	/**
	 * @param factory builds the message in the entry's locale
	 * @return this builder
	 */
	public Audit withLocalizedMessage(Function<DiscordLocale, MessageCreateData> factory) {
		this.message = factory;
		return this;
	}

	/**
	 * @param factory builds the embed in the entry's locale
	 * @return this builder
	 */
	public Audit withLocalizedEmbed(Function<DiscordLocale, MessageEmbed> factory) {
		this.message = locale -> MessageCreateData.fromEmbeds(factory.apply(locale));
		return this;
	}

	/**
	 * @param factory builds the text in the entry's locale
	 * @return this builder
	 */
	public Audit withLocalizedText(Function<DiscordLocale, String> factory) {
		this.message = locale -> MessageCreateData.fromContent(factory.apply(locale));
		return this;
	}

	/**
	 * Sets a text whose placeholders ({@code <user>}) take the values given with {@link #with(String, Object)}.
	 *
	 * @param template the text
	 * @return this builder
	 */
	public Audit withText(String template) {
		this.message = ignored -> MessageCreateData.fromContent(replacePlaceholders(template));
		return this;
	}

	/**
	 * Builds an embed that starts from the kind the severity picks.
	 *
	 * @param embedBuilder fills in the embed
	 * @return this builder
	 */
	public Audit withEmbed(Consumer<EmbedBuilder> embedBuilder) {
		EmbedBuilder builder = severityEmbed();
		embedBuilder.accept(builder);
		MessageEmbed embed = builder.build();
		this.message = ignored -> MessageCreateData.fromEmbeds(embed);
		return this;
	}

	/**
	 * Sets an embed as it is, without the severity's kind.
	 *
	 * @param embed the embed
	 * @return this builder
	 */
	public Audit withEmbed(MessageEmbed embed) {
		this.message = ignored -> MessageCreateData.fromEmbeds(embed);
		return this;
	}

	/**
	 * @param message the message, as it is
	 * @return this builder
	 */
	public Audit withMessage(MessageCreateData message) {
		this.message = ignored -> message;
		return this;
	}

	/**
	 * Posts the entry. One that is not posted, or has no message, completes at once.
	 *
	 * @return completes once the message is posted, or failed to be
	 */
	public CompletableFuture<Void> send() {
		if (auditService == null || message == null || !auditService.isRecorded(category, entry))
			return CompletableFuture.completedFuture(null);

		return auditService.audit(category, entry, message.apply(locale()));
	}

	private DiscordLocale locale() {
		if (locale != null) return locale;
		if (settingsProvider != null && settingsProvider.getIfAvailable() != null) return settingsProvider.getObject().getLocale();

		return DiscordLocale.ENGLISH_US;
	}

	private String replacePlaceholders(String template) {
		String result = template;
		for (Map.Entry<String, Object> placeholder : placeholders.entrySet())
			result = result.replace("<" + placeholder.getKey() + ">", String.valueOf(placeholder.getValue()));

		return result;
	}

	private EmbedBuilder severityEmbed() {
		return switch (severity) {
			case WARNING -> StyleKit.embeds().warning();
			case ERROR -> StyleKit.embeds().error();
			default -> StyleKit.embeds().info();
		};
	}
}
