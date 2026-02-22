package me.whereareiam.yui.model.journey.session;

import lombok.Getter;
import me.whereareiam.yui.type.journey.JourneyPolicyDecisionType;
import me.whereareiam.yui.type.journey.JourneyStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mutable runtime session for one journey participant execution.
 *
 * @param <S> journey state type
 */
@Getter
@SuppressWarnings("unused")
public final class JourneySession<S> {
	private final @NotNull String id;
	private final @NotNull String journeyId;

	private final long participantId;

	private final @NotNull Payload<S> payload;
	private final @NotNull Lifecycle lifecycle;
	private final @NotNull Runtime runtime;

	/**
	 * Creates a new running session.
	 *
	 * @param id            unique session id
	 * @param journeyId     journey identifier
	 * @param participantId participant identifier
	 * @param stateType     journey state class
	 * @param state         journey state object
	 * @param attributes    immutable typed attributes
	 * @param storeId       backing store identifier
	 */
	public JourneySession(
			@NotNull String id,
			@NotNull String journeyId,
			long participantId,
			@NotNull Class<S> stateType,
			@NotNull S state,
			@Nullable JourneyAttributes attributes,
			@NotNull String storeId
	) {
		this.id = id;
		this.journeyId = journeyId;
		this.participantId = participantId;
		this.payload = new Payload<>(state, stateType, attributes);
		this.lifecycle = new Lifecycle();
		this.runtime = new Runtime(storeId);
	}

	/**
	 * @return typed state object
	 */
	public @NotNull S getState() {
		return payload.state;
	}

	/**
	 * Returns state cast to requested type.
	 *
	 * @param stateType requested state type
	 * @param <T>       state type
	 * @return typed state
	 */
	@SuppressWarnings("unchecked")
	public <T> @NotNull T getState(@NotNull Class<T> stateType) {
		if (!stateType.isAssignableFrom(payload.stateType))
			throw new IllegalStateException("Journey session state type mismatch: requested="
					+ stateType.getName() + ", actual=" + payload.stateType.getName());

		return (T) payload.state;
	}

	public void setStatus(@NotNull JourneyStatus status) {
		lifecycle.status = status;
		touch();
	}

	public void setCurrentStepId(@Nullable String currentStepId) {
		lifecycle.currentStepId = currentStepId;
		touch();
	}

	/**
	 * Refreshes the session update timestamp.
	 */
	public void touch() {
		lifecycle.updatedAt = Instant.now();
	}

	/**
	 * Session payload data.
	 *
	 * @param <S> journey state type
	 */
	@Getter
	public static final class Payload<S> {
		private final @NotNull S state;
		private final @NotNull Class<S> stateType;
		private final @NotNull JourneyAttributes attributes;

		private Payload(
				@NotNull S state,
				@NotNull Class<S> stateType,
				@Nullable JourneyAttributes attributes
		) {
			this.state = state;
			this.stateType = stateType;
			this.attributes = attributes == null ? JourneyAttributes.empty() : attributes;
		}
	}

	/**
	 * Session lifecycle values.
	 */
	@Getter
	public static final class Lifecycle {
		private @Nullable String currentStepId;
		private @NotNull JourneyStatus status = JourneyStatus.RUNNING;
		private final @NotNull Instant startedAt = Instant.now();
		private @NotNull Instant updatedAt = startedAt;
	}

	/**
	 * Runtime-only engine data.
	 */
	@Getter
	public static final class Runtime {
		private final @NotNull String storeId;
		private final @NotNull Map<String, JourneyPolicyDecisionType> groupDecisions = new ConcurrentHashMap<>();
		private final @NotNull Set<String> enteredSteps = ConcurrentHashMap.newKeySet();

		private Runtime(@NotNull String storeId) {
			this.storeId = storeId;
		}
	}
}
