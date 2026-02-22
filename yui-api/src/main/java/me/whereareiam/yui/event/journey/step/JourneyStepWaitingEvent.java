package me.whereareiam.yui.event.journey.step;

import lombok.Getter;
import me.whereareiam.yui.event.journey.base.JourneyEvent;
import me.whereareiam.yui.model.journey.session.JourneySession;
import org.jetbrains.annotations.NotNull;

/**
 * Published when a journey waits for a signal on the current step.
 */
@Getter
public final class JourneyStepWaitingEvent extends JourneyEvent {
	private final @NotNull String stepId;

	/**
	 * Creates a waiting-for-signal event.
	 *
	 * @param session current session
	 * @param stepId waiting step id
	 */
	public JourneyStepWaitingEvent(@NotNull JourneySession<?> session, @NotNull String stepId) {
		super(session);
		this.stepId = stepId;
	}
}
