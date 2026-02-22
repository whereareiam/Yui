package me.whereareiam.yui.event.journey;

import me.whereareiam.yui.event.journey.base.JourneyEvent;
import me.whereareiam.yui.model.journey.session.JourneySession;
import org.jetbrains.annotations.NotNull;

/**
 * Published when a journey fails.
 */
public final class JourneyFailedEvent extends JourneyEvent {
	/**
	 * Creates a failed event.
	 *
	 * @param session failed session
	 */
	public JourneyFailedEvent(@NotNull JourneySession<?> session) {
		super(session);
	}
}
