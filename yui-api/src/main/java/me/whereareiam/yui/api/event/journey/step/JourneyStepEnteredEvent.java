package me.whereareiam.yui.api.event.journey.step;

import lombok.Getter;
import me.whereareiam.yui.api.event.journey.base.JourneyEvent;
import me.whereareiam.yui.api.model.journey.session.JourneySession;
import org.jetbrains.annotations.NotNull;

/**
 * Published when a journey step is entered.
 */
@Getter
public final class JourneyStepEnteredEvent extends JourneyEvent {
	private final @NotNull String stepId;

	/**
	 * Creates a step-entered event.
	 *
	 * @param session current session
	 * @param stepId entered step id
	 */
	public JourneyStepEnteredEvent(@NotNull JourneySession<?> session, @NotNull String stepId) {
		super(session);
		this.stepId = stepId;
	}
}
