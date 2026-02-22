package me.whereareiam.yui.event.journey.session;

import me.whereareiam.yui.event.journey.base.JourneyEvent;
import me.whereareiam.yui.model.journey.session.JourneySession;
import org.jetbrains.annotations.NotNull;

/**
 * Published when a journey session is started.
 */
public final class JourneySessionStartedEvent extends JourneyEvent {
	/**
	 * Creates a started event.
	 *
	 * @param session started session
	 */
	public JourneySessionStartedEvent(@NotNull JourneySession<?> session) {
		super(session);
	}
}
