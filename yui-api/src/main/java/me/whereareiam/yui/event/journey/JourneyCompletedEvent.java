package me.whereareiam.yui.event.journey;

import me.whereareiam.yui.event.journey.base.JourneyEvent;
import me.whereareiam.yui.model.journey.session.JourneySession;
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
