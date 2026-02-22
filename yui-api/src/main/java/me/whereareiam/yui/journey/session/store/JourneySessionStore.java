package me.whereareiam.yui.journey.session.store;

import me.whereareiam.yui.model.journey.session.JourneySession;
import me.whereareiam.yui.type.journey.JourneyStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

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
	 * Finds a session for a journey participant pair in the given statuses.
	 *
	 * @param journeyId journey id
	 * @param participantId participant id
	 * @param statuses acceptable statuses
	 * @return optional session
	 */
	@NotNull Optional<JourneySession<?>> find(
			@NotNull String journeyId,
			long participantId,
			@NotNull Set<JourneyStatus> statuses
	);

	/**
	 * Finds sessions for a journey in the given statuses.
	 *
	 * @param journeyId journey id
	 * @param statuses acceptable statuses
	 * @return session collection
	 */
	@NotNull Collection<JourneySession<?>> findAll(
			@NotNull String journeyId,
			@NotNull Set<JourneyStatus> statuses
	);

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
