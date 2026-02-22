package me.whereareiam.yui.journey;

import me.whereareiam.yui.model.journey.JourneySignal;
import me.whereareiam.yui.model.journey.session.JourneySession;
import me.whereareiam.yui.model.journey.session.JourneySessionRequest;
import me.whereareiam.yui.type.journey.JourneyStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * High-level orchestration service for journey lifecycle operations.
 */
@SuppressWarnings("unused")
public interface JourneyService {
	/**
	 * Starts a journey for a participant.
	 *
	 * @param request start request containing journey id, participant id and state
	 * @param <S> journey state type
	 * @return created or existing active session
	 */
	<S> @NotNull JourneySession<S> start(@NotNull JourneySessionRequest<S> request);

	/**
	 * Delivers a signal to an active journey session.
	 *
	 * @param sessionId target session id
	 * @param stateType requested state type
	 * @param signal signal payload
	 * @param <S> journey state type
	 * @return updated session
	 */
	<S> @NotNull JourneySession<S> signal(
			@NotNull String sessionId,
			@NotNull Class<S> stateType,
			@NotNull JourneySignal signal
	);

	/**
	 * Finds a journey session by id.
	 *
	 * @param sessionId session id
	 * @param stateType requested state type
	 * @param <S> journey state type
	 * @return optional session
	 */
	<S> @NotNull Optional<JourneySession<S>> findSession(
			@Nullable String sessionId,
			@NotNull Class<S> stateType
	);

	/**
	 * Finds a session for the given journey and participant in the given statuses.
	 *
	 * @param journeyId journey id
	 * @param participantId participant id
	 * @param statuses acceptable statuses
	 * @param stateType requested state type
	 * @param <S> journey state type
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
	 * Cancels an active session.
	 *
	 * @param sessionId target session id
	 * @return true when session was found and cancellation was applied
	 */
	boolean cancel(@Nullable String sessionId);
}
