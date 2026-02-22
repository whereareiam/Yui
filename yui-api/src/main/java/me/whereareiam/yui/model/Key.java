package me.whereareiam.yui.model;

import org.jetbrains.annotations.NotNull;

/**
 * Typed key for reusable type-safe key/value containers.
 *
 * @param <T> key value type
 */
public final class Key<T> {
	private final @NotNull String name;
	private final @NotNull Class<T> type;

	private Key(@NotNull String name, @NotNull Class<T> type) {
		if (name.isBlank())
			throw new IllegalArgumentException("Key name cannot be blank");

		this.name = name;
		this.type = type;
	}

	public static <T> @NotNull Key<T> of(@NotNull String name, @NotNull Class<T> type) {
		return new Key<>(name, type);
	}

	public @NotNull String getName() {
		return name;
	}

	public @NotNull Class<T> getType() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Key<?> other))
			return false;

		return name.equals(other.name) && type.equals(other.type);
	}

	@Override
	public int hashCode() {
		return 31 * name.hashCode() + type.hashCode();
	}
}
