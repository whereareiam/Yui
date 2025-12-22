package me.whereareiam.yui.common.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.service.AuditService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

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
		return getChannelId(auditType)
				.map(jda::getTextChannelById)
				.map(channel -> sendToChannel(channel, auditType, message))
				.orElseGet(() -> {
					log.trace("No audit channel configured for type: {}", auditType);
					return CompletableFuture.completedFuture(null);
				});
	}

	@Override
	public boolean isConfigured(String auditType) {
		return getChannelId(auditType).isPresent();
	}

	@Override
	public Optional<String> getChannelId(String auditType) {
		Settings settings = settingsProvider.getObject();

		Map<String, String> auditConfig = settings.getDiscord().getChannels().getAudit();
		if (auditConfig == null)
			return Optional.empty();

		return Optional.ofNullable(auditConfig.get(auditType));
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
