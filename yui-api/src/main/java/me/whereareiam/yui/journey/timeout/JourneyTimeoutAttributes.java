package me.whereareiam.yui.journey.timeout;

import me.whereareiam.yui.model.Key;
import org.jetbrains.annotations.NotNull;

/**
 * Typed keys used by timeout subsystem.
 */
public final class JourneyTimeoutAttributes {
	public static final @NotNull Key<Long> TIMEOUT_SECONDS = Key.of("journey.timeout.seconds", Long.class);

	private JourneyTimeoutAttributes() {
		throw new IllegalStateException("Utility class");
	}
}
