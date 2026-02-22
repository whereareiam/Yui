package me.whereareiam.yui.event.journey.step;

import lombok.Getter;
import me.whereareiam.yui.event.journey.base.JourneyEvent;
import me.whereareiam.yui.model.journey.session.JourneySession;
import org.jetbrains.annotations.NotNull;

/**
 * Published when a journey step completes.
 */
@Getter
public final class JourneyStepCompletedEvent extends JourneyEvent {
	private final @NotNull String stepId;

	/**
	 * Creates a step-completed event.
	 *
	 * @param session current session
	 * @param stepId completed step id
	 */
	public JourneyStepCompletedEvent(@NotNull JourneySession<?> session, @NotNull String stepId) {
		super(session);
		this.stepId = stepId;
	}
}
