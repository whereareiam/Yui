package me.whereareiam.yui.common.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.configura.type.MultiValue;
import me.whereareiam.yui.util.translation.LocaleScopedValues;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.service.AuditService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultAuditService implements AuditService {
	private final ObjectProvider<Settings> settingsProvider;
	private final JDA jda;

	@Override
	public CompletableFuture<Void> audit(String auditType, String message) {
		return audit(auditType, MessageCreateData.fromContent(message));
	}

	@Override
	public CompletableFuture<Void> audit(String auditType, MessageEmbed embed) {
		return audit(auditType, MessageCreateData.fromEmbeds(embed));
	}

	@Override
	public CompletableFuture<Void> audit(String auditType, MessageCreateData message) {
		return audit(auditType, message, null);
	}

	@Override
	public CompletableFuture<Void> audit(String auditType, MessageCreateData message, DiscordLocale locale) {
		List<String> channelIds = getChannelIds(auditType, locale).orElse(List.of());
		if (channelIds.isEmpty()) {
						log.trace("No audit channel configured for type: {}", auditType);
						return CompletableFuture.completedFuture(null);
		}

		// Send to all configured channels; complete when all attempts are done
		List<CompletableFuture<Void>> futures = channelIds.stream()
				.map(jda::getTextChannelById)
				.map(channel -> sendToChannel(channel, auditType, message))
				.toList();

		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}

	@Override
	public boolean isConfigured(String auditType) {
		return getChannelIds(auditType, null).map(list -> !list.isEmpty()).orElse(false);
	}

	@Override
	public Optional<List<String>> getChannelIds(String auditType, DiscordLocale locale) {
		Settings settings = settingsProvider.getObject();

		Map<String, MultiValue<String>> auditConfig = settings.getDiscord().getChannels().getAudit();
		if (auditConfig == null)
			return Optional.empty();

		// Try exact match first
		MultiValue<String> exactMatch = auditConfig.get(auditType);
		if (exactMatch != null) {
			return Optional.of(LocaleScopedValues.resolve(exactMatch, locale));
		}

		// Try wildcard patterns
		// Patterns like "update_plugin_*_available" should match "update_plugin_yuiverification_available"
		for (Map.Entry<String, MultiValue<String>> entry : auditConfig.entrySet()) {
			String pattern = entry.getKey();
			if (pattern.contains("*") && matchesWildcard(auditType, pattern)) {
				return Optional.of(LocaleScopedValues.resolve(entry.getValue(), locale));
			}
		}

		return Optional.empty();
	}

	/**
	 * Checks if an audit type matches a wildcard pattern.
	 * Supports * as a wildcard character.
	 *
	 * @param auditType the audit type to check
	 * @param pattern the pattern with wildcards
	 * @return true if the audit type matches the pattern
	 */
	private boolean matchesWildcard(String auditType, String pattern) {
		// Convert wildcard pattern to regex
		// Escape special regex characters except *
		// Note: We don't escape backslash because patterns shouldn't contain them,
		// and escaping it would interfere with our own escape sequences
		String regex = pattern
				.replace(".", "\\.")
				.replace("$", "\\$")
				.replace("^", "\\^")
				.replace("+", "\\+")
				.replace("?", "\\?")
				.replace("(", "\\(")
				.replace(")", "\\)")
				.replace("[", "\\[")
				.replace("]", "\\]")
				.replace("{", "\\{")
				.replace("}", "\\}")
				.replace("|", "\\|")
				.replace("*", ".*"); // Replace * with .*

		return auditType.matches(regex);
	}

	@Override
	public List<DiscordLocale> getConfiguredLocales(String auditType) {
		Settings settings = settingsProvider.getObject();
		Map<String, MultiValue<String>> auditConfig = settings.getDiscord().getChannels().getAudit();
		if (auditConfig == null)
			return List.of();

		MultiValue<String> mv = auditConfig.get(auditType);
		if (mv == null)
			return List.of();

		return LocaleScopedValues.extractLocales(mv.asList());
	}

	private CompletableFuture<Void> sendToChannel(TextChannel channel, String auditType, MessageCreateData message) {
		if (channel == null) {
			log.warn("Audit channel not found for type: {}", auditType);
			return CompletableFuture.completedFuture(null);
		}

		return channel.sendMessage(message)
				.submit()
				.thenApply(_ -> (Void) null)
				.exceptionally(throwable -> {
					log.error("Failed to send audit message for type: {}", auditType, throwable);
					return null;
				});
	}
}
