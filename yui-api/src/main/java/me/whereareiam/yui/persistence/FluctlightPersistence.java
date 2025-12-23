package me.whereareiam.yui.persistence;

import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.model.fluctlight.FluctlightData;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.Optional;

/**
 * Service interface for Fluctlight database operations.
 * <p>
 * This service handles database persistence operations for Fluctlight data.
 * It does not handle caching, JDA User retrieval, or business logic.
 * <p>
 * <b>Implementation Note:</b> By default, all write operations are batched
 * and asynchronous for performance. Use {@link #flush()} to ensure all
 * pending writes are completed (e.g., during shutdown or in tests).
 */
@SuppressWarnings("unused")
public interface FluctlightPersistence {
	/**
	 * Loads data for a fluctlight from the database.
	 *
	 * @param fluctlight The Fluctlight instance
	 * @return Optional containing the data if found, empty otherwise
	 */
	Optional<FluctlightData> loadData(Fluctlight fluctlight);

	/**
	 * Saves data for a fluctlight to the database.
	 * Creates a new entry if it doesn't exist, or updates an existing one.
	 *
	 * @param fluctlight The Fluctlight instance
	 * @param data The data to save
	 */
	void saveData(Fluctlight fluctlight, FluctlightData data);

	/**
	 * Deletes a FluctlightEntity by fluctlight.
	 *
	 * @param fluctlight The Fluctlight instance to delete
	 */
	void deleteById(Fluctlight fluctlight);

	/**
	 * Checks if a FluctlightEntity exists for the given fluctlight.
	 *
	 * @param fluctlight The Fluctlight instance to check
	 * @return true if the entity exists, false otherwise
	 */
	boolean existsById(Fluctlight fluctlight);

	/**
	 * Updates the primary language for a fluctlight.
	 *
	 * @param fluctlight The Fluctlight instance
	 * @param locale The new primary language locale
	 */
	void updatePrimaryLanguage(Fluctlight fluctlight, DiscordLocale locale);

	/**
	 * Adds an additional language for a fluctlight.
	 *
	 * @param fluctlight The Fluctlight instance
	 * @param locale The additional language locale to add
	 */
	void addAdditionalLanguage(Fluctlight fluctlight, DiscordLocale locale);

	/**
	 * Removes an additional language for a fluctlight.
	 *
	 * @param fluctlight The Fluctlight instance
	 * @param locale The additional language locale to remove
	 */
	void removeAdditionalLanguage(Fluctlight fluctlight, DiscordLocale locale);

	/**
	 * Replaces all additional languages for a fluctlight.
	 *
	 * @param fluctlight The Fluctlight instance
	 * @param locales The new additional language locales
	 */
	void updateAdditionalLanguages(Fluctlight fluctlight, DiscordLocale[] locales);

	/**
	 * Adds an allowed role for a fluctlight.
	 * These are framework roles that the bot is allowed to work with,
	 * not the fluctlight's guild roles.
	 *
	 * @param fluctlight The Fluctlight instance
	 * @param roleId The allowed role ID to add
	 */
	void addAllowedRole(Fluctlight fluctlight, long roleId);

	/**
	 * Removes an allowed role for a fluctlight.
	 * These are framework roles that the bot is allowed to work with,
	 * not the fluctlight's guild roles.
	 *
	 * @param fluctlight The Fluctlight instance
	 * @param roleId The allowed role ID to remove
	 */
	void removeAllowedRole(Fluctlight fluctlight, long roleId);

	/**
	 * Flushes all pending write operations to ensure they complete.
	 * <p>
	 * By default, persistence operations are batched and asynchronous.
	 * Call this method when you need to ensure all pending writes have
	 * completed, such as:
	 * <ul>
	 *   <li>During application shutdown</li>
	 *   <li>In integration tests</li>
	 *   <li>Before critical operations that depend on persisted state</li>
	 * </ul>
	 * This method blocks until all pending operations complete.
	 */
	default void flush() {
		// Default: no-op for implementations that don't queue
	}
}