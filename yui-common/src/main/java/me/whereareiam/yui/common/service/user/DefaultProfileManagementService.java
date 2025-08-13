package me.whereareiam.yui.common.service.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.event.user.UserProfileClearedEvent;
import me.whereareiam.yui.api.output.provider.UserProfileCacheProvider;
import me.whereareiam.yui.api.output.service.UserProfileService;
import me.whereareiam.yui.api.output.service.ProfileManagementService;
import me.whereareiam.yui.api.model.profile.UserProfile;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for managing profile-related business operations that involve
 * multiple profile service calls or complex workflows.
 * 
 * This service orchestrates operations that go beyond simple CRUD operations
 * and handles business logic for profile management.
 */
@Slf4j
@Service
@AllArgsConstructor
public class DefaultProfileManagementService implements ProfileManagementService {
	private final UserProfileService userProfileService;
	private final UserProfileCacheProvider cacheProvider;
	private final ApplicationEventPublisher eventPublisher;

	/**
	 * Clears a user's profile completely and reinitializes it.
	 * This operation removes the profile from both cache and database,
	 * then creates a fresh, empty profile.
	 *
	 * @param userId The ID of the user whose profile should be cleared
	 * @return The newly created profile, or empty if the operation failed
	 */
	public Optional<UserProfile> clearAndReinitializeProfile(long userId) {
		try {
			log.info("Clearing and reinitializing profile for user: {}", userId);
			
			// Get the old profile before clearing (for the event)
			Optional<UserProfile> oldProfileOpt = userProfileService.getProfile(userId);
			
			// First, clear from cache
			cacheProvider.evictProfile(userId);
			log.debug("Evicted user {} from cache", userId);
			
			// Delete from database
			userProfileService.deleteProfile(userId);
			log.debug("Deleted user {} profile from database", userId);
			
			// Create fresh profile
			Optional<UserProfile> newProfile = userProfileService.createProfile(userId);
			if (newProfile.isPresent()) {
				log.info("Successfully reinitialized profile for user: {}", userId);
				
				// Publish the event
				UserProfile oldProfile = oldProfileOpt.orElse(null);
				UserProfileClearedEvent event = new UserProfileClearedEvent(userId, oldProfile, newProfile.get());
				eventPublisher.publishEvent(event);
				
				// Check if the event was cancelled
				if (event.isCancelled()) {
					log.warn("Profile clear operation was cancelled by event listener for user: {}", userId);
					// Rollback: delete the new profile and restore the old one if possible
					userProfileService.deleteProfile(userId);
					if (oldProfile != null) {
						// Recreate the old profile
						userProfileService.createProfile(oldProfile);
						cacheProvider.putProfile(userId, oldProfile);
					}
					return Optional.empty();
				}
				
				return newProfile;
			} else {
				log.error("Failed to create new profile for user: {}", userId);
				return Optional.empty();
			}
		} catch (Exception e) {
			log.error("Error during profile clear and reinitialization for user: {}", userId, e);
			return Optional.empty();
		}
	}

	/**
	 * Checks if a user profile exists in the system.
	 *
	 * @param userId The ID of the user to check
	 * @return true if the profile exists, false otherwise
	 */
	public boolean profileExists(long userId) {
		return userProfileService.getProfile(userId).isPresent();
	}

	/**
	 * Gets a user profile, creating it if it doesn't exist.
	 *
	 * @param userId The ID of the user
	 * @return The user profile, either existing or newly created
	 */
	public UserProfile getOrCreateProfile(long userId) {
		return userProfileService.getProfile(userId)
				.orElseGet(() -> {
					log.debug("Profile not found for user {}, creating new one", userId);
					return userProfileService.createProfile(userId)
							.orElseThrow(() -> new IllegalStateException("Failed to create profile for user: " + userId));
				});
	}
}
