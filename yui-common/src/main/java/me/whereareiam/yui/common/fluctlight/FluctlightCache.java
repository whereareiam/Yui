package me.whereareiam.yui.common.fluctlight;

import me.whereareiam.yui.api.model.fluctlight.Fluctlight;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Keeps the users read last, up to a fixed number; the one read longest ago leaves first. A changed user leaves once
 * the change is committed, so the next read loads what was saved.
 */
@Component
public class FluctlightCache {
	static final int CAPACITY = 10_000;

	private final Map<Long, Fluctlight> entries = new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<Long, Fluctlight> eldest) {
			return size() > CAPACITY;
		}
	};

	/**
	 * Returns the cached user.
	 *
	 * @param userId the Discord id of the user
	 * @return the user, or empty when not cached
	 */
	public synchronized @NotNull Optional<Fluctlight> find(long userId) {
		return Optional.ofNullable(entries.get(userId));
	}

	/**
	 * Caches a user, replacing an earlier snapshot.
	 *
	 * @param fluctlight the user
	 */
	public synchronized void put(@NotNull Fluctlight fluctlight) {
		entries.put(fluctlight.getId(), fluctlight);
	}

	/**
	 * Drops a user from the cache.
	 *
	 * @param userId the Discord id of the user
	 */
	public synchronized void invalidate(long userId) {
		entries.remove(userId);
	}

	/**
	 * Drops a user from the cache once the current transaction commits, or now when none is active.
	 *
	 * @param userId the Discord id of the user
	 */
	public void invalidateAfterCommit(long userId) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			invalidate(userId);
			return;
		}

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				invalidate(userId);
			}
		});
	}

	synchronized int size() {
		return entries.size();
	}
}
