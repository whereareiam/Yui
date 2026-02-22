package me.whereareiam.yui.model.journey.session;

import me.whereareiam.yui.model.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable typed attribute bag for journey session payload data.
 */
public final class JourneyAttributes {
	private static final @NotNull JourneyAttributes EMPTY = new JourneyAttributes(Map.of());

	private final @NotNull Map<Key<?>, Object> values;

	private JourneyAttributes(@NotNull Map<Key<?>, Object> values) {
		this.values = Map.copyOf(values);
	}

	public static @NotNull JourneyAttributes empty() {
		return EMPTY;
	}

	public static @NotNull Builder builder() {
		return new Builder();
	}

	public <T> @NotNull Optional<T> get(@NotNull Key<T> key) {
		Object value = values.get(key);
		if (value == null)
			return Optional.empty();

		return Optional.of(key.getType().cast(value));
	}

	public boolean contains(@NotNull Key<?> key) {
		return values.containsKey(key);
	}

	public <T> @NotNull JourneyAttributes with(@NotNull Key<T> key, @Nullable T value) {
		Map<Key<?>, Object> copy = new LinkedHashMap<>(values);
		if (value == null) {
			copy.remove(key);
		} else {
			validateType(key, value);
			copy.put(key, value);
		}

		if (copy.isEmpty())
			return empty();

		return new JourneyAttributes(copy);
	}

	private static <T> void validateType(@NotNull Key<T> key, @NotNull Object value) {
		if (!key.getType().isInstance(value))
			throw new IllegalArgumentException("Journey attribute value type mismatch for key '" + key.getName() + "'");
	}

	public static final class Builder {
		private final @NotNull Map<Key<?>, Object> values = new LinkedHashMap<>();

		public <T> @NotNull Builder put(@NotNull Key<T> key, @Nullable T value) {
			if (value == null) {
				values.remove(key);
				return this;
			}

			validateType(key, value);
			values.put(key, value);
			return this;
		}

		public @NotNull JourneyAttributes build() {
			if (values.isEmpty())
				return empty();

			return new JourneyAttributes(values);
		}
	}
}
