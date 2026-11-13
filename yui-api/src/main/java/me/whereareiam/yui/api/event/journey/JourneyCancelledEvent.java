package me.whereareiam.yui.api.event.journey;

import me.whereareiam.yui.api.event.journey.base.JourneyEvent;
import me.whereareiam.yui.api.model.journey.session.JourneySession;
import org.jetbrains.annotations.NotNull;

/**
 * Published when a journey is cancelled.
 */
public final class JourneyCancelledEvent extends JourneyEvent {
	/**
	 * Creates a cancelled event.
	 *
	 * @param session cancelled session
	 */
	public JourneyCancelledEvent(@NotNull JourneySession<?> session) {
		super(session);
	}
}
