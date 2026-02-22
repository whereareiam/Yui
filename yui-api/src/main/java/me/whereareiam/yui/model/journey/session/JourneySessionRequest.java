package me.whereareiam.yui.model.journey.session;

import lombok.Getter;
import me.whereareiam.yui.model.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable request payload for starting a journey.
 *
 * @param <S> journey state type
 */
@Getter
@SuppressWarnings("unused")
public final class JourneySessionRequest<S> {
	private final @NotNull String journeyId;
	private final long participantId;
	private final @NotNull Payload<S> payload;
	private final @NotNull Settings settings;

	private JourneySessionRequest(@NotNull Builder<S> builder) {
		this.journeyId = builder.journeyId;
		this.participantId = builder.participantId;
		this.payload = Payload.<S>builder()
				.stateType(builder.stateType)
				.state(builder.state)
				.attributes(builder.attributes)
				.build();
		this.settings = Settings.builder()
				.sessionStore(builder.sessionStore)
				.build();
	}

	/**
	 * Creates a builder with required values.
	 *
	 * @param journeyId journey identifier
	 * @param participantId participant identifier
	 * @param stateType journey state class
	 * @param state journey state
	 * @param <S> journey state type
	 * @return request builder
	 */
	public static <S> @NotNull Builder<S> builder(
			@NotNull String journeyId,
			long participantId,
			@NotNull Class<S> stateType,
			@NotNull S state
	) {
		return new Builder<>(journeyId, participantId, stateType, state);
	}

	/**
	 * Builder for {@link JourneySessionRequest}.
	 *
	 * @param <S> journey state type
	 */
	public static final class Builder<S> {
		private final @NotNull String journeyId;
		private final long participantId;
		private final @NotNull Class<S> stateType;
		private final @NotNull S state;
		private @Nullable JourneyAttributes attributes;
		private @Nullable String sessionStore;

		private Builder(@NotNull String journeyId, long participantId, @NotNull Class<S> stateType, @NotNull S state) {
			this.journeyId = journeyId;
			this.participantId = participantId;
			this.stateType = stateType;
			this.state = state;
		}

		/**
		 * Sets request attributes.
		 *
		 * @param attributes typed attributes, nullable
		 * @return this builder
		 */
		public @NotNull Builder<S> attributes(@Nullable JourneyAttributes attributes) {
			this.attributes = attributes;
			return this;
		}

		/**
		 * Adds one typed request attribute.
		 *
		 * @param key attribute key
		 * @param value attribute value, nullable to clear key
		 * @param <T> attribute type
		 * @return this builder
		 */
		public <T> @NotNull Builder<S> attribute(
				@NotNull Key<T> key,
				@Nullable T value
		) {
			JourneyAttributes current = attributes == null ? JourneyAttributes.empty() : attributes;
			this.attributes = current.with(key, value);
			return this;
		}

		/**
		 * Sets session store override id.
		 *
		 * @param sessionStore store id, nullable
		 * @return this builder
		 */
		public @NotNull Builder<S> sessionStore(@Nullable String sessionStore) {
			this.sessionStore = sessionStore;
			return this;
		}

		/**
		 * Builds the immutable start request.
		 *
		 * @return start request
		 */
		public @NotNull JourneySessionRequest<S> build() {
			return new JourneySessionRequest<>(this);
		}
	}

	/**
	 * @return journey state class
	 */
	public @NotNull Class<S> getStateType() {
		return payload.getStateType();
	}

	/**
	 * @return journey state object
	 */
	public @NotNull S getState() {
		return payload.getState();
	}

	/**
	 * @return immutable typed attributes
	 */
	public @NotNull JourneyAttributes getAttributes() {
		return payload.getAttributes();
	}

	/**
	 * @return session store override id, nullable
	 */
	public @Nullable String getSessionStore() {
		return settings.getSessionStore();
	}

	/**
	 * Request payload values.
	 *
	 * @param <S> journey state type
	 */
	@Getter
	public static final class Payload<S> {
		private final @NotNull Class<S> stateType;
		private final @NotNull S state;
		private final @NotNull JourneyAttributes attributes;

		@lombok.Builder
		private Payload(
				@NotNull Class<S> stateType,
				@NotNull S state,
				@Nullable JourneyAttributes attributes
		) {
			this.stateType = stateType;
			this.state = state;
			this.attributes = attributes == null ? JourneyAttributes.empty() : attributes;
		}
	}

	/**
	 * Request startup settings.
	 */
	@Getter
	public static final class Settings {
		private final @Nullable String sessionStore;

		@lombok.Builder
		private Settings(@Nullable String sessionStore) {
			this.sessionStore = sessionStore;
		}
	}
}
