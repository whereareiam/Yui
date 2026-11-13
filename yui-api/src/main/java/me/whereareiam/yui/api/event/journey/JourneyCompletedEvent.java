package me.whereareiam.yui.api.event.journey;

import me.whereareiam.yui.api.event.journey.base.JourneyEvent;
import me.whereareiam.yui.api.model.journey.session.JourneySession;
import org.jetbrains.annotations.NotNull;

/**
 * Published when a journey finishes successfully.
 */
public final class JourneyCompletedEvent extends JourneyEvent {
	/**
	 * Creates a completed event.
	 *
	 * @param session completed session
	 */
	public JourneyCompletedEvent(@NotNull JourneySession<?> session) {
		super(session);
	}
}
