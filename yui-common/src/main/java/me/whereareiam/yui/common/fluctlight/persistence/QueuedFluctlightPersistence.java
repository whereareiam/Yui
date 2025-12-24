package me.whereareiam.yui.common.fluctlight.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.common.config.provider.RolesProvider;
import me.whereareiam.yui.model.config.roles.Roles;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.model.fluctlight.FluctlightData;
import me.whereareiam.yui.model.ManagedRole;
import me.whereareiam.yui.persistence.FluctlightPersistence;
import me.whereareiam.yui.service.RoleService;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * Asynchronous decorator for FluctlightPersistence that batches write operations.
 * <p>
 * This implementation wraps the actual persistence layer (e.g., database adapter)
 * and queues all write operations for batch processing. This prevents database
 * slowness from blocking user interactions and reduces database load through batching.
 * <p>
 * Read operations ({@link #loadData}, {@link #existsById}) are delegated directly
 * to the underlying persistence layer without queuing.
 * <p>
 * This is the <b>default</b> FluctlightPersistence implementation injected throughout
 * the application via the {@link Primary} annotation.
 */
@Slf4j
@Service
@Primary
public class QueuedFluctlightPersistence implements FluctlightPersistence {
	private final FluctlightPersistence delegate;
	private final RolesProvider rolesProvider;
	private final RoleService roleService;

	private final BlockingQueue<PersistenceTask> queue = new LinkedBlockingQueue<>();

	public QueuedFluctlightPersistence(
			@Qualifier("fluctlightPersistenceAdapter") FluctlightPersistence delegate,
			RolesProvider rolesProvider,
			RoleService roleService
	) {
		this.delegate = delegate;
		this.rolesProvider = rolesProvider;
		this.roleService = roleService;
	}

	// ========== Read Operations (Direct Delegation) ==========

	@Override
	public Optional<FluctlightData> loadData(Fluctlight fluctlight) {
		return delegate.loadData(fluctlight);
	}

	@Override
	public boolean existsById(Fluctlight fluctlight) {
		return delegate.existsById(fluctlight);
	}

	// ========== Write Operations (Queued) ==========

	@Override
	public void saveData(Fluctlight fluctlight, FluctlightData data) {
		queuePersistence(fluctlight);
	}

	@Override
	public void deleteById(Fluctlight fluctlight) {
		// Deletion is rare and should be immediate
		delegate.deleteById(fluctlight);
	}

	@Override
	public void updatePrimaryLanguage(Fluctlight fluctlight, DiscordLocale locale) {
		queuePersistence(fluctlight);
	}

	@Override
	public void addAdditionalLanguage(Fluctlight fluctlight, DiscordLocale locale) {
		queuePersistence(fluctlight);
	}

	@Override
	public void removeAdditionalLanguage(Fluctlight fluctlight, DiscordLocale locale) {
		queuePersistence(fluctlight);
	}

	@Override
	public void updateAdditionalLanguages(Fluctlight fluctlight, DiscordLocale[] locales) {
		queuePersistence(fluctlight);
	}

	@Override
	public void addAllowedRole(Fluctlight fluctlight, long roleId) {
		queuePersistence(fluctlight);
	}

	@Override
	public void removeAllowedRole(Fluctlight fluctlight, long roleId) {
		queuePersistence(fluctlight);
	}

	@Override
	public void flush() {
		log.debug("Flushing {} pending persistence operations", queue.size());
		
		// Drain all pending tasks
		List<PersistenceTask> remaining = new ArrayList<>();
		queue.drainTo(remaining);
		
		if (remaining.isEmpty()) {
			return;
		}
		
		// Process them synchronously
		for (PersistenceTask task : remaining) {
			try {
				persistRoles(task);
			} catch (Exception e) {
				log.error("Failed to flush persistence for user {}", task.getFluctlight().getId(), e);
			}
		}
		
		log.debug("Flushed all pending persistence operations");
	}

	/**
	 * Queues a persistence operation for a Fluctlight.
	 * This method is non-blocking and returns immediately.
	 *
	 * @param fluctlight The Fluctlight to persist
	 */
	private void queuePersistence(Fluctlight fluctlight) {
		queue.offer(new PersistenceTask(fluctlight));
		log.trace("Queued persistence for user {}", fluctlight.getId());
	}

	/**
	 * Processes the persistence queue periodically.
	 * Runs every 100ms and processes up to the configured batch size.
	 */
	@Scheduled(fixedDelay = 100)
	private void processQueue() {
		if (queue.isEmpty()) return;

		Roles rolesConfig = rolesProvider.get();
		if (rolesConfig == null || rolesConfig.getSettings() == null) return;

		int batchSize = rolesConfig.getSettings().getPersistenceBatchSize();

		// Drain up to batchSize tasks from the queue
		List<PersistenceTask> batch = new ArrayList<>();
		queue.drainTo(batch, batchSize);

		if (batch.isEmpty()) return;

		// Process batch asynchronously to avoid blocking the scheduler
		processBatchAsync(batch);
	}

	/**
	 * Processes a batch of persistence tasks asynchronously using virtual threads.
	 * <p>
	 * Uses lightweight virtual threads (Project Loom) which are perfect for I/O-bound
	 * database operations. Virtual threads don't consume OS threads when blocked,
	 * allowing thousands of concurrent persistence operations without resource exhaustion.
	 *
	 * @param batch The batch of tasks to process
	 */
	private void processBatchAsync(List<PersistenceTask> batch) {
		// Execute in a virtual thread - lightweight and perfect for I/O-bound DB writes
		Thread.ofVirtual()
				.name("fluctlight-persistence-", 0)
				.start(() -> {
					for (PersistenceTask task : batch) {
						try {
							persistRoles(task);
						} catch (Exception e) {
							log.error("Failed to persist roles for user {}", task.getFluctlight().getId(), e);
							// Could implement retry logic here if needed
						}
					}
					log.debug("Persisted {} role updates to database", batch.size());
				});
	}

	/**
	 * Persists roles for a single task.
	 * Only persists config roles (not temporary/API-added roles).
	 *
	 * @param task The persistence task
	 */
	private void persistRoles(PersistenceTask task) {
		Fluctlight fluctlight = task.getFluctlight();

		// Filter out temporary roles before persisting
		// Only config roles should be persisted to database
		long[] rolesToPersist = filterConfigRoles(fluctlight.getAllowedRoles());

		try {
			delegate.saveData(fluctlight, new FluctlightData(
					fluctlight.getPrimaryLanguage(),
					fluctlight.getAdditionalLanguages(),
					rolesToPersist
			));

			int originalCount = fluctlight.getAllowedRoles() != null ? fluctlight.getAllowedRoles().length : 0;
			int persistedCount = rolesToPersist != null ? rolesToPersist.length : 0;
			if (originalCount > persistedCount) {
				log.trace("Persisted roles for user {}: {} roles ({} temporary roles filtered out)", 
					fluctlight.getId(), persistedCount, originalCount - persistedCount);
				return;
			}

			log.trace("Persisted roles for user {}: {} roles", fluctlight.getId(), persistedCount);
		} catch (Exception e) {
			log.error("Error persisting roles for user {}", fluctlight.getId(), e);
			throw e;
		}
	}

	/**
	 * Filters out temporary/API-added roles, keeping only config roles.
	 * Temporary roles are runtime-only and should not be persisted.
	 * <p>
	 * Uses {@link ManagedRole#isPersistable()} to determine which roles
	 * should be persisted to the database.
	 *
	 * @param allRoles All roles including temporary ones
	 * @return Only config roles that should be persisted
	 */
	private long[] filterConfigRoles(long[] allRoles) {
		if (allRoles == null || allRoles.length == 0) return allRoles;

		// Get all managed roles and build set of persistable role IDs
		Set<Long> persistableRoleIds = roleService.getAllRoles().stream()
				.filter(ManagedRole::isPersistable)
				.map(ManagedRole::getId)
				.collect(Collectors.toSet());

		// Only keep roles that are persistable (config roles)
		return Arrays.stream(allRoles)
				.filter(persistableRoleIds::contains)
				.toArray();
	}

	/**
	 * Represents a pending persistence operation.
	 */
	@Getter
	@AllArgsConstructor
	private static class PersistenceTask {
		private final Fluctlight fluctlight;
	}
}
