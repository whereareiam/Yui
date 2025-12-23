package me.whereareiam.yui.common.role.sync;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Tracks pending role synchronization changes for a single user.
 * <p>
 * This class collects role add/remove operations and optimizes them
 * before applying to Discord. For example, if a role is added and then
 * removed before sync happens, both operations are cancelled.
 */
@Getter
@RequiredArgsConstructor
public class PendingRoleSync {
	private final long userId;
	private final Set<Long> rolesToAdd = new HashSet<>();
	private final Set<Long> rolesToRemove = new HashSet<>();
	private volatile Instant lastModified = Instant.now();
	private volatile boolean isProcessing = false;

	/**
	 * Adds a role to the pending add set.
	 * If the role is in the remove set, it's removed from there (cancellation).
	 *
	 * @param roleId The role ID to add
	 */
	public synchronized void addRole(long roleId) {
		// If previously marked for removal, cancel that instead
		if (rolesToRemove.remove(roleId)) {
			// Just cancelled a remove, don't add to add set (unless it wasn't there)
			// Add to add set to show final intent
			rolesToAdd.add(roleId);
		} else {
			// Not in remove set, just add
			rolesToAdd.add(roleId);
		}
		lastModified = Instant.now();
	}

	/**
	 * Adds a role to the pending remove set.
	 * If the role is in the add set, it's removed from there (complete cancellation).
	 *
	 * @param roleId The role ID to remove
	 */
	public synchronized void removeRole(long roleId) {
		// If previously marked for addition, just cancel that (don't add to remove set)
		if (!rolesToAdd.remove(roleId)) {
			// Not in add set, so add to remove set
			rolesToRemove.add(roleId);
		}
		// If it was in add set, we just removed it - complete cancellation
		lastModified = Instant.now();
	}

	/**
	 * Checks if this sync is ready to be processed based on debounce time.
	 *
	 * @param debounceMillis Milliseconds to wait after last modification
	 * @return true if ready to process, false otherwise
	 */
	public synchronized boolean isReadyToProcess(long debounceMillis) {
		if (isProcessing) return false;
		if (rolesToAdd.isEmpty() && rolesToRemove.isEmpty()) return false;

		long quietTime = Duration.between(lastModified, Instant.now()).toMillis();
		return quietTime >= debounceMillis;
	}

	/**
	 * Marks this sync as being processed.
	 */
	public synchronized void markProcessing() {
		isProcessing = true;
	}

	/**
	 * Checks if there are any pending changes.
	 *
	 * @return true if there are changes to process
	 */
	public synchronized boolean hasPendingChanges() {
		return !rolesToAdd.isEmpty() || !rolesToRemove.isEmpty();
	}

	/**
	 * Gets a snapshot of roles to add and clears the internal set.
	 *
	 * @return Set of role IDs to add
	 */
	public synchronized Set<Long> consumeRolesToAdd() {
		Set<Long> snapshot = new HashSet<>(rolesToAdd);
		rolesToAdd.clear();
		return snapshot;
	}

	/**
	 * Gets a snapshot of roles to remove and clears the internal set.
	 *
	 * @return Set of role IDs to remove
	 */
	public synchronized Set<Long> consumeRolesToRemove() {
		Set<Long> snapshot = new HashSet<>(rolesToRemove);
		rolesToRemove.clear();
		return snapshot;
	}
}
