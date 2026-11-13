package me.whereareiam.yui.common.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.model.config.audit.AuditTrail;
import me.whereareiam.yui.api.persistence.AuditPersistence;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;

/**
 * Removes kept audit entries older than {@code store.keep}, once an hour. Without {@code keep}, entries stay.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditRetention {
	private final ObjectProvider<AuditTrail> trail;
	private final ObjectProvider<AuditPersistence> persistence;
	private final Clock clock;

	@Scheduled(fixedDelayString = "PT1H", initialDelayString = "PT1M")
	void removeOld() {
		AuditTrail.Store store = trail.getObject().getStore();
		AuditPersistence kept = persistence.getIfAvailable();
		if (!store.isEnabled() || store.getKeep() == null || kept == null) return;

		int removed = kept.removeBefore(clock.instant().minusSeconds(store.getKeep().getSeconds()));
		if (removed > 0) log.info("[Audit] Removed {} kept entries older than {}", removed, store.getKeep());
	}
}
