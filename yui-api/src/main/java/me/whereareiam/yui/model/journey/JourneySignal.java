package me.whereareiam.yui.model.journey;

import lombok.Getter;
import me.whereareiam.yui.model.Key;
import me.whereareiam.yui.model.journey.session.JourneyAttributes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * External signal delivered to a waiting journey step.
 */
@Getter
@SuppressWarnings("unused")
public final class JourneySignal {
	private final @NotNull String type;
	private final @NotNull JourneyAttributes attributes;
	private final @Nullable Long actorId;

	public JourneySignal(
			@NotNull String type,
			@Nullable JourneyAttributes attributes,
			@Nullable Long actorId
	) {
		if (type.isBlank()) throw new IllegalArgumentException("Signal type cannot be null or blank");

		this.type = type;
		this.attributes = attributes == null ? JourneyAttributes.empty() : attributes;
		this.actorId = actorId;
	}

	/**
	 * Creates a signal without attributes and actor metadata.
	 *
	 * @param type signal type
	 * @return signal instance
	 */
	public static @NotNull JourneySignal of(@NotNull String type) {
		return new JourneySignal(type, JourneyAttributes.empty(), null);
	}

	/**
	 * Creates a signal with typed attributes and no actor metadata.
	 *
	 * @param type signal type
	 * @param attributes signal attributes
	 * @return signal instance
	 */
	public static @NotNull JourneySignal of(
			@NotNull String type,
			@Nullable JourneyAttributes attributes
	) {
		return new JourneySignal(type, attributes, null);
	}

	public static @NotNull Builder builder(@NotNull String type) {
		return new Builder(type);
	}

	public <T> @NotNull Optional<T> attribute(@NotNull Key<T> key) {
		return attributes.get(key);
	}

	public static final class Builder {
		private final @NotNull String type;
		private @Nullable JourneyAttributes attributes;
		private @Nullable Long actorId;

		private Builder(@NotNull String type) {
			this.type = type;
		}

		public @NotNull Builder attributes(@Nullable JourneyAttributes attributes) {
			this.attributes = attributes;
			return this;
		}

		public <T> @NotNull Builder attribute(
				@NotNull Key<T> key,
				@Nullable T value
		) {
			JourneyAttributes current = attributes == null ? JourneyAttributes.empty() : attributes;
			this.attributes = current.with(key, value);
			return this;
		}

		public @NotNull Builder actorId(@Nullable Long actorId) {
			this.actorId = actorId;
			return this;
		}

		public @NotNull JourneySignal build() {
			return new JourneySignal(type, attributes, actorId);
		}
	}
}
