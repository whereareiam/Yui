package me.whereareiam.yui.journey.session.store;

import me.whereareiam.yui.model.journey.session.JourneySession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

/**
 * Storage contract for journey sessions.
 */
public interface JourneySessionStore {
	/**
	 * @return unique store id
	 */
	@NotNull String getId();

	/**
	 * Loads a session by id.
	 *
	 * @param sessionId session id
	 * @return optional session
	 */
	@NotNull Optional<JourneySession<?>> get(@Nullable String sessionId);

	/**
	 * Finds active session for journey participant pair.
	 *
	 * @param journeyId journey id
	 * @param participantId participant id
	 * @return optional active session
	 */
	@NotNull Optional<JourneySession<?>> findActive(@Nullable String journeyId, long participantId);

	/**
	 * Persists a session.
	 *
	 * @param session session to persist
	 */
	void save(@NotNull JourneySession<?> session);

	/**
	 * Deletes a session by id.
	 *
	 * @param sessionId session id
	 */
	void delete(@Nullable String sessionId);

	/**
	 * Returns all sessions from this store.
	 *
	 * @return session collection
	 */
	@NotNull Collection<JourneySession<?>> all();
}
