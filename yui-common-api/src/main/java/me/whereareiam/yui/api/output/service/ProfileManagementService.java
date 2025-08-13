package me.whereareiam.yui.api.output.service;

import me.whereareiam.yui.api.model.profile.UserProfile;

import java.util.Optional;

/**
 * Service interface for managing profile-related business operations that involve
 * multiple profile service calls or complex workflows.
 * 
 * This service orchestrates operations that go beyond simple CRUD operations
 * and handles business logic for profile management.
 */
public interface ProfileManagementService {
	
	/**
	 * Clears a user's profile completely and reinitializes it.
	 * This operation removes the profile from both cache and database,
	 * then creates a fresh, empty profile.
	 *
	 * @param userId The ID of the user whose profile should be cleared
	 * @return The newly created profile, or empty if the operation failed
	 */
	Optional<UserProfile> clearAndReinitializeProfile(long userId);

	/**
	 * Checks if a user profile exists in the system.
	 *
	 * @param userId The ID of the user to check
	 * @return true if the profile exists, false otherwise
	 */
	boolean profileExists(long userId);

	/**
	 * Gets a user profile, creating it if it doesn't exist.
	 *
	 * @param userId The ID of the user
	 * @return The user profile, either existing or newly created
	 */
	UserProfile getOrCreateProfile(long userId);
}
