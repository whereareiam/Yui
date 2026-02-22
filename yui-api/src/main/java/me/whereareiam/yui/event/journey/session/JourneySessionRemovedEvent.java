package me.whereareiam.yui.event.journey.session;

import me.whereareiam.yui.event.journey.base.JourneyEvent;
import me.whereareiam.yui.model.journey.session.JourneySession;
import org.jetbrains.annotations.NotNull;

/**
 * Published when a journey session is removed from store.
 */
public final class JourneySessionRemovedEvent extends JourneyEvent {
	/**
	 * Creates a removed event.
	 *
	 * @param session removed session
	 */
	public JourneySessionRemovedEvent(@NotNull JourneySession<?> session) {
		super(session);
	}
}
