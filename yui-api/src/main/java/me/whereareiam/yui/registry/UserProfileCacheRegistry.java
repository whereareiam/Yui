package me.whereareiam.yui.registry;

import me.whereareiam.yui.model.profile.UserProfile;
import net.dv8tion.jda.api.entities.User;

import java.util.Optional;

/**
 * A simple interface that keeps "userprofile" data pinned to user IDs.
 * JDA manages the actual User references, so we just store UserProfile objects.
 */
public interface UserProfileCacheRegistry {

	/**
	 * Store or update a user’s UserProfile in the cache.
	 */
	void putProfile(long userId, UserProfile userProfile);

	/**
	 * Get the cached UserProfile for a user ID, if present.
	 */
	Optional<UserProfile> getProfile(long userId);

	/**
	 * Evict a user’s UserProfile from the cache (if they leave a guild, etc.).
	 */
	void evictProfile(long userId);

	/**
	 * Return the JDA User from the bot’s cache, if available.
	 */
	Optional<User> getJdaUser(long userId);
}