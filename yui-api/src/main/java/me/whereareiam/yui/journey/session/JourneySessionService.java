package me.whereareiam.yui.journey.session;

import me.whereareiam.yui.model.journey.definition.JourneyDefinition;
import me.whereareiam.yui.model.journey.session.JourneySession;
import me.whereareiam.yui.model.journey.session.JourneySessionRequest;
import me.whereareiam.yui.type.journey.JourneyStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Primary session surface for journey operations.
 */
public interface JourneySessionService {
	/**
	 * Finds a session by id and casts it to the requested state type.
	 *
	 * @param sessionId session id
	 * @param stateType expected state type
	 * @param <S> state type
	 * @return optional session
	 */
	<S> @NotNull Optional<JourneySession<S>> findSession(@Nullable String sessionId, @NotNull Class<S> stateType);

	/**
	 * Finds a session by id without state casting.
	 *
	 * @param sessionId session id
	 * @return optional session
	 */
	@NotNull Optional<JourneySession<?>> findAny(@Nullable String sessionId);

	/**
	 * Finds a session for the given journey and participant in the given statuses.
	 *
	 * @param journeyId journey id
	 * @param participantId participant id
	 * @param statuses acceptable statuses
	 * @param stateType expected state type
	 * @param <S> state type
	 * @return optional session
	 */
	<S> @NotNull Optional<JourneySession<S>> find(
			@NotNull String journeyId,
			long participantId,
			@NotNull Set<JourneyStatus> statuses,
			@NotNull Class<S> stateType
	);

	/**
	 * Finds sessions for the given journey in the given statuses.
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
	 * Creates and persists a new session for the request.
	 *
	 * @param request session request
	 * @param definition journey definition
	 * @param <S> state type
	 * @return created session
	 */
	<S> @NotNull JourneySession<S> create(
			@NotNull JourneySessionRequest<S> request,
			@NotNull JourneyDefinition<Object> definition
	);

	/**
	 * Persists the session.
	 *
	 * @param session session to persist
	 */
	void save(@NotNull JourneySession<?> session);

	/**
	 * Deletes the session.
	 *
	 * @param session session to delete
	 */
	void delete(@NotNull JourneySession<?> session);

	/**
	 * Executes a supplier under a session lock.
	 *
	 * @param sessionId session id
	 * @param supplier supplier to execute
	 * @param <T> return type
	 * @return supplier result
	 */
	<T> T withLock(@NotNull String sessionId, @NotNull SessionSupplier<T> supplier);

	/**
	 * Executes a runnable under a session lock.
	 *
	 * @param sessionId session id
	 * @param runnable runnable to execute
	 */
	void withLock(@NotNull String sessionId, @NotNull SessionRunnable runnable);

	/**
	 * Supplier for locked execution.
	 *
	 * @param <T> return type
	 */
	@FunctionalInterface
	interface SessionSupplier<T> {
		T get() throws Exception;
	}

	/**
	 * Runnable for locked execution.
	 */
	@FunctionalInterface
	interface SessionRunnable {
		void run() throws Exception;
	}
}
