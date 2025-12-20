package me.whereareiam.yui.persistence;

import me.whereareiam.yui.model.fluctlight.FluctlightData;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.Optional;

/**
 * Service interface for Fluctlight database operations.
 * <p>
 * This service handles ONLY database persistence operations for Fluctlight data.
 * It does not handle caching, JDA User retrieval, or business logic.
 */
public interface FluctlightPersistence {
	/**
	 * Loads data for a fluctlight from the database.
	 *
	 * @param userId The fluctlight ID to load data for
	 * @return Optional containing the data if found, empty otherwise
	 */
	Optional<FluctlightData> loadData(long userId);

	/**
	 * Saves data for a fluctlight to the database.
	 * Creates a new entry if it doesn't exist, or updates an existing one.
	 *
	 * @param userId The fluctlight ID
	 * @param data The data to save
	 */
	void saveData(long userId, FluctlightData data);

	/**
	 * Deletes a FluctlightEntity by fluctlight ID.
	 *
	 * @param userId The fluctlight ID to delete
	 */
	void deleteById(long userId);

	/**
	 * Checks if a FluctlightEntity exists for the given fluctlight ID.
	 *
	 * @param userId The fluctlight ID to check
	 * @return true if the entity exists, false otherwise
	 */
	boolean existsById(long userId);

	/**
	 * Updates the primary language for a fluctlight.
	 *
	 * @param userId The fluctlight ID
	 * @param locale The new primary language locale
	 */
	void updatePrimaryLanguage(long userId, DiscordLocale locale);

	/**
	 * Adds an additional language for a fluctlight.
	 *
	 * @param userId The fluctlight ID
	 * @param locale The additional language locale to add
	 */
	void addAdditionalLanguage(long userId, DiscordLocale locale);

	/**
	 * Removes an additional language for a fluctlight.
	 *
	 * @param userId The fluctlight ID
	 * @param locale The additional language locale to remove
	 */
	void removeAdditionalLanguage(long userId, DiscordLocale locale);

	/**
	 * Adds an allowed role for a fluctlight.
	 * These are framework roles that the bot is allowed to work with,
	 * not the fluctlight's guild roles.
	 *
	 * @param userId The fluctlight ID
	 * @param roleId The allowed role ID to add
	 */
	void addAllowedRole(long userId, long roleId);

	/**
	 * Removes an allowed role for a fluctlight.
	 * These are framework roles that the bot is allowed to work with,
	 * not the fluctlight's guild roles.
	 *
	 * @param userId The fluctlight ID
	 * @param roleId The allowed role ID to remove
	 */
	void removeAllowedRole(long userId, long roleId);
}