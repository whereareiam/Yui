package me.whereareiam.yui.event.journey.base;

import lombok.Getter;
import me.whereareiam.yui.model.journey.session.JourneySession;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * Base type for all journey lifecycle events.
 */
@Getter
public abstract class JourneyEvent {
	private final @NotNull JourneySession<?> session;
	private final @NotNull Instant occurredAt;

	/**
	 * Creates a new event instance for the given session.
	 *
	 * @param session journey session
	 */
	protected JourneyEvent(@NotNull JourneySession<?> session) {
		this.session = session;
		this.occurredAt = Instant.now();
	}
}
