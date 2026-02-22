package me.whereareiam.yui.journey;

import me.whereareiam.yui.model.Key;
import me.whereareiam.yui.model.component.ComponentAttributes;
import org.jetbrains.annotations.NotNull;

/**
 * Shared typed keys for journey-related metadata.
 */
public final class JourneyKeys {
	/**
	 * Timeout seconds attribute key for journey session metadata.
	 */
	public static final @NotNull Key<Long> TIMEOUT_SECONDS = Key.of("journey.timeout.seconds", Long.class);

	/**
	 * Session id attribute key for journey-bound components.
	 */
	public static final @NotNull Key<String> SESSION_ID = Key.of("journey.session.id", String.class);

	/**
	 * Creates component attributes bound to the given session id.
	 *
	 * @param sessionId journey session id
	 * @return component attributes with session id
	 */
	public static @NotNull ComponentAttributes forSession(@NotNull String sessionId) {
		return ComponentAttributes.builder()
				.put(SESSION_ID, sessionId)
				.build();
	}
}
