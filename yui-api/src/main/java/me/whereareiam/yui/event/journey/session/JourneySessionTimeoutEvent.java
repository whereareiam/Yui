package me.whereareiam.yui.event.journey.session;

import lombok.Getter;
import me.whereareiam.yui.event.journey.base.JourneyEvent;
import me.whereareiam.yui.model.journey.session.JourneySession;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * Published when a journey session timeout is triggered by timeout lifecycle.
 */
@Getter
public final class JourneySessionTimeoutEvent extends JourneyEvent {
	private final long timeoutSeconds;
	private final @NotNull Duration elapsed;

	/**
	 * Creates a timeout event.
	 *
	 * @param session timed out session
	 * @param timeoutSeconds effective timeout in seconds
	 * @param elapsed elapsed runtime duration
	 */
	public JourneySessionTimeoutEvent(
			@NotNull JourneySession<?> session,
			long timeoutSeconds,
			@NotNull Duration elapsed
	) {
		super(session);
		this.timeoutSeconds = timeoutSeconds;
		this.elapsed = elapsed;
	}
}
