package me.whereareiam.yui.api.event.fluctlight;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.model.fluctlight.Fluctlight;

/**
 * Published when Yui stores a user it had never seen.
 * <p>
 * Listen with {@link org.springframework.transaction.event.TransactionalEventListener} to receive it once the change
 * is committed. A failing listener is logged and does not undo the change.
 */
@Getter
@RequiredArgsConstructor
public class FluctlightCreatedEvent {
	/**
	 * The new user.
	 */
	private final Fluctlight fluctlight;
}
