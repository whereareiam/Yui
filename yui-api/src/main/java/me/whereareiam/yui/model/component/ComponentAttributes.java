package me.whereareiam.yui.model.component;

import me.whereareiam.yui.model.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable typed attribute bag for component metadata.
 */
public final class ComponentAttributes {
	private static final @NotNull ComponentAttributes EMPTY = new ComponentAttributes(Map.of());

	private final @NotNull Map<Key<?>, Object> values;

	private ComponentAttributes(@NotNull Map<Key<?>, Object> values) {
		this.values = Map.copyOf(values);
	}

	/**
	 * Returns an empty attribute bag.
	 *
	 * @return empty attributes
	 */
	public static @NotNull ComponentAttributes empty() {
		return EMPTY;
	}

	/**
	 * Creates a new builder for component attributes.
	 *
	 * @return attributes builder
	 */
	public static @NotNull Builder builder() {
		return new Builder();
	}

	/**
	 * Returns the typed value for the given key.
	 *
	 * @param key attribute key
	 * @param <T> attribute value type
	 * @return optional value
	 */
	public <T> @NotNull Optional<T> get(@NotNull Key<T> key) {
		Object value = values.get(key);
		if (value == null) return Optional.empty();

		return Optional.of(key.getType().cast(value));
	}

	/**
	 * Returns whether a key is present.
	 *
	 * @param key attribute key
	 * @return true when present
	 */
	public boolean contains(@NotNull Key<?> key) {
		return values.containsKey(key);
	}

	/**
	 * Returns whether the bag is empty.
	 *
	 * @return true when no attributes are stored
	 */
	public boolean isEmpty() {
		return values.isEmpty();
	}

	/**
	 * Returns a copy with the provided key updated or removed.
	 *
	 * @param key attribute key
	 * @param value attribute value, nullable to remove
	 * @param <T> attribute value type
	 * @return new attribute bag
	 */
	public <T> @NotNull ComponentAttributes with(@NotNull Key<T> key, @Nullable T value) {
		Map<Key<?>, Object> copy = new LinkedHashMap<>(values);
		if (value == null) {
			copy.remove(key);
		} else {
			validateType(key, value);
			copy.put(key, value);
		}

		if (copy.isEmpty()) return empty();
		return new ComponentAttributes(copy);
	}

	private static <T> void validateType(@NotNull Key<T> key, @NotNull Object value) {
		if (!key.getType().isInstance(value))
			throw new IllegalArgumentException("Component attribute value type mismatch for key '" + key.getName() + "'");
	}

	/**
	 * Builder for component attributes.
	 */
	public static final class Builder {
		private final @NotNull Map<Key<?>, Object> values = new LinkedHashMap<>();

		/**
		 * Adds or removes a value for the given key.
		 *
		 * @param key attribute key
		 * @param value attribute value, nullable to remove
		 * @param <T> attribute value type
		 * @return this builder
		 */
		public <T> @NotNull Builder put(@NotNull Key<T> key, @Nullable T value) {
			if (value == null) {
				values.remove(key);
				return this;
			}

			validateType(key, value);
			values.put(key, value);
			return this;
		}

		/**
		 * Builds the immutable attribute bag.
		 *
		 * @return component attributes
		 */
		public @NotNull ComponentAttributes build() {
			if (values.isEmpty()) return empty();

			return new ComponentAttributes(values);
		}
	}
}
