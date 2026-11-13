package me.whereareiam.yui.common.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.model.config.audit.AuditTrail;
import me.whereareiam.yui.service.AuditService;
import me.whereareiam.yui.type.audit.AuditCategory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Posts audit entries to the channel {@code audit.yml} gives their category, or the category the file sends the
 * entry to. A channel that cannot be found or written to is logged; the entry is then lost.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultAuditService implements AuditService {
	private final ObjectProvider<AuditTrail> trail;
	private final JDA jda;

	@Override
	public @NotNull CompletableFuture<Void> audit(@NotNull AuditCategory category, @NotNull String entry, @NotNull MessageCreateData message) {
		Optional<String> channelId = channel(category, entry);
		if (channelId.isEmpty()) return CompletableFuture.completedFuture(null);

		TextChannel channel = jda.getTextChannelById(channelId.get());
		if (channel == null) {
			log.warn("[Audit] Channel {} of {} entries is not found, {} is not posted", channelId.get(), category, entry);
			return CompletableFuture.completedFuture(null);
		}

		return channel.sendMessage(message)
				.submit()
				.thenApply(ignored -> (Void) null)
				.exceptionally(error -> {
					log.error("[Audit] Could not post {} to channel {}", entry, channelId.get(), error);
					return null;
				});
	}

	@Override
	public @NotNull Optional<String> channel(@NotNull AuditCategory category, @NotNull String entry) {
		return trail.getObject().channel(category, entry).filter(channel -> !channel.isBlank());
	}
}
