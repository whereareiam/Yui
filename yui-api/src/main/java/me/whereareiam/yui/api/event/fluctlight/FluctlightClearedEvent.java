package me.whereareiam.yui.api.event.fluctlight;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.model.fluctlight.Fluctlight;

/**
 * Published after everything Yui knows about a user was reset.
 * <p>
 * Listen with {@link org.springframework.transaction.event.TransactionalEventListener} to receive it once the change
 * is committed. A failing listener is logged and does not undo the change.
 */
@Getter
@RequiredArgsConstructor
public class FluctlightClearedEvent {
	/**
	 * The user before the reset.
	 */
	private final Fluctlight previous;

	/**
	 * The user after the reset.
	 */
	private final Fluctlight fluctlight;
}
