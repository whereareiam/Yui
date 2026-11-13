package me.whereareiam.yui.common.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.model.config.audit.AuditTrail;
import me.whereareiam.yui.api.persistence.AuditPersistence;
import me.whereareiam.yui.api.service.AuditService;
import me.whereareiam.yui.api.type.audit.AuditCategory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Posts audit entries to the channel {@code audit.yml} gives their category, or the category the file sends the
 * entry to, and keeps them in the database when {@code store} is on. A channel that cannot be found or written to is
 * logged; the entry is then not posted.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultAuditService implements AuditService {
	private final ObjectProvider<AuditTrail> trail;
	private final ObjectProvider<AuditPersistence> persistence;
	private final JDA jda;
	private final Clock clock;

	@Override
	public @NotNull CompletableFuture<Void> audit(@NotNull AuditCategory category, @NotNull String entry, @NotNull MessageCreateData message) {
		if (trail.getObject().stores(entry)) keep(category, entry, message);

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
	public boolean isRecorded(@NotNull AuditCategory category, @NotNull String entry) {
		return isPosted(category, entry) || trail.getObject().stores(entry);
	}

	private void keep(AuditCategory category, String entry, MessageCreateData message) {
		AuditPersistence store = persistence.getIfAvailable();
		if (store == null) return;

		try {
			store.save(new AuditPersistence.Record(category, entry, clock.instant(), AuditText.of(message)));
		} catch (RuntimeException exception) {
			log.error("[Audit] Could not keep {}", entry, exception);
		}
	}

	@Override
	public @NotNull Optional<String> channel(@NotNull AuditCategory category, @NotNull String entry) {
		return trail.getObject().channel(category, entry).filter(channel -> !channel.isBlank());
	}
}
