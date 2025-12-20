package me.whereareiam.yui.fluctlight;

import me.whereareiam.yui.model.fluctlight.Fluctlight;

import java.util.Optional;

/**
 * Registry interface for managing Fluctlight instances in memory.
 * <p>
 * Fluctlight instances are primarily kept in memory for fast access.
 * This registry manages the in-memory storage of Fluctlight objects.
 * <p>
 * This is an internal interface and should not be used directly by API consumers.
 */
public interface FluctlightRegistry {
	/**
	 * Store or update a Fluctlight in the registry.
	 *
	 * @param userId    The fluctlight ID
	 * @param fluctlight The Fluctlight instance to store
	 */
	void putFluctlight(long userId, Fluctlight fluctlight);

	/**
	 * Get the Fluctlight for a fluctlight ID, if present.
	 *
	 * @param userId The fluctlight ID to look up
	 * @return Optional containing the Fluctlight if present, empty otherwise
	 */
	Optional<Fluctlight> getFluctlight(long userId);

	/**
	 * Remove a Fluctlight from the registry (e.g., when a fluctlight leaves a guild).
	 *
	 * @param userId The fluctlight ID to remove
	 */
	void evictFluctlight(long userId);
}