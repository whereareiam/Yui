package me.whereareiam.yui.event.fluctlight;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.whereareiam.yui.event.Cancellable;
import me.whereareiam.yui.model.fluctlight.Fluctlight;

/**
 * Published before everything Yui knows about a user is reset.
 * <p>
 * Published inside the change's transaction, before anything is saved. Listeners may only veto by cancelling it;
 * side effects belong to the event published after the change.
 */
@Getter
@RequiredArgsConstructor
public class FluctlightClearEvent implements Cancellable {
	/**
	 * The user before the change.
	 */
	private final Fluctlight fluctlight;

	@Setter
	private boolean cancelled;
}
