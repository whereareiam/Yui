package me.whereareiam.yui.api.event.journey;

import me.whereareiam.yui.api.event.journey.base.JourneyEvent;
import me.whereareiam.yui.api.model.journey.session.JourneySession;
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
