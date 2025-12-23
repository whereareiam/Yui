package me.whereareiam.yui.fluctlight;

import me.whereareiam.yui.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.Optional;

/**
 * Service interface for managing Fluctlight business operations.
 * <p>
 * This service orchestrates operations that involve cache, database, and JDA User retrieval.
 * It handles the complete lifecycle of Fluctlight instances, including eager loading
 * from the database when first encountered.
 */
@SuppressWarnings("unused")
public interface FluctlightService {
	/**
	 * Gets a Fluctlight for a fluctlight, loading it eagerly from the database if needed.
	 * <p>
	 * This is the primary method for accessing Fluctlight instances. It:
	 * 1. Checks the cache first
	 * 2. If not cached, gets JDA User from JDA
	 * 3. Loads custom data from database (eager loading)
	 * 4. Creates Fluctlight instance and caches it
	 *
	 * @param userId The fluctlight ID
	 * @return Optional containing the Fluctlight if the fluctlight exists in JDA, empty otherwise
	 */
	Optional<Fluctlight> get(long userId);

	/**
	 * Gets a Fluctlight, creating it if it doesn't exist.
	 * <p>
	 * If the Fluctlight doesn't exist in cache or database, it will be created
	 * with default values (empty custom data).
	 *
	 * @param userId The fluctlight ID
	 * @return The Fluctlight instance (never empty, will be created if needed)
	 * @throws IllegalStateException if the fluctlight doesn't exist in JDA
	 */
	Fluctlight getOrCreate(long userId);

	/**
	 * Saves a Fluctlight to both cache and database.
	 * <p>
	 * Updates the in-memory cache and persists custom data to the database.
	 *
	 * @param fluctlight The Fluctlight to save
	 */
	void save(Fluctlight fluctlight);

	/**
	 * Checks if a Fluctlight exists in the system (cache or database).
	 *
	 * @param userId The fluctlight ID to check
	 * @return true if the Fluctlight exists, false otherwise
	 */
	boolean exists(long userId);

	/**
	 * Clears a Fluctlight completely and reinitializes it.
	 * <p>
	 * This operation removes the Fluctlight from both cache and database,
	 * then creates a fresh, empty Fluctlight.
	 *
	 * @param userId The ID of the fluctlight whose Fluctlight should be cleared
	 * @return The newly created Fluctlight, or empty if the operation failed
	 */
	Optional<Fluctlight> clear(long userId);

	/**
	 * Updates the primary language for a Fluctlight.
	 * <p>
	 * This method handles persistence and event publishing.
	 *
	 * @param fluctlight The Fluctlight instance
	 * @param locale The new primary language locale
	 */
	void updatePrimaryLanguage(Fluctlight fluctlight, DiscordLocale locale);

	/**
	 * Adds an additional language for a Fluctlight.
	 * <p>
	 * This method handles persistence and event publishing.
	 *
	 * @param fluctlight The Fluctlight instance
	 * @param locale The additional language locale to add
	 */
	void addAdditionalLanguage(Fluctlight fluctlight, DiscordLocale locale);

	/**
	 * Removes an additional language for a Fluctlight.
	 * <p>
	 * This method handles persistence and event publishing.
	 *
	 * @param fluctlight The Fluctlight instance
	 * @param locale The additional language locale to remove
	 */
	void removeAdditionalLanguage(Fluctlight fluctlight, DiscordLocale locale);

	/**
	 * Replaces all additional languages for a Fluctlight in a single operation.
	 * <p>
	 * This method handles persistence and event publishing.
	 *
	 * @param fluctlight The Fluctlight instance
	 * @param locales The new set of additional language locales
	 */
	void setAdditionalLanguages(Fluctlight fluctlight, DiscordLocale[] locales);

	/**
	 * Adds an allowed role for a Fluctlight.
	 * <p>
	 * This method handles persistence.
	 *
	 * @param fluctlight The Fluctlight instance
	 * @param roleId The allowed role ID to add
	 */
	void addAllowedRole(Fluctlight fluctlight, long roleId);

	/**
	 * Removes an allowed role for a Fluctlight.
	 * <p>
	 * This method handles persistence.
	 *
	 * @param fluctlight The Fluctlight instance
	 * @param roleId The allowed role ID to remove
	 */
	void removeAllowedRole(Fluctlight fluctlight, long roleId);
}