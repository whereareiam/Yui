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

		return Optional.ofNullable(auditConfig.get(auditType))
				.map(mv -> LocaleScopedValues.resolve(mv, locale));
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
