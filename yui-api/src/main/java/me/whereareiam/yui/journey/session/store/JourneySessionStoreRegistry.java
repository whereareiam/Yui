package me.whereareiam.yui.journey.session.store;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

/**
 * Registry for available journey session stores.
 */
public interface JourneySessionStoreRegistry {
	/**
	 * Registers a session store.
	 *
	 * @param store store instance
	 */
	void register(@NotNull JourneySessionStore store);

	/**
	 * Looks up a store by id.
	 *
	 * @param id store id
	 * @return optional store
	 */
	@NotNull Optional<JourneySessionStore> get(@Nullable String id);

	/**
	 * Resolves a store for runtime usage.
	 *
	 * @param id store id
	 * @return resolved store
	 */
	@NotNull JourneySessionStore resolve(@Nullable String id);

	/**
	 * Returns all registered stores.
	 *
	 * @return store collection
	 */
	@NotNull Collection<JourneySessionStore> all();
}
