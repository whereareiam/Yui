package me.whereareiam.yui.event.fluctlight;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.event.Cancellable;
import me.whereareiam.yui.model.fluctlight.Fluctlight;

/**
 * Event published before a fluctlight's Fluctlight is cleared and reinitialized.
 * This event can be cancelled to prevent the Fluctlight clearing operation.
 */
@Getter
@RequiredArgsConstructor
public class FluctlightClearEvent implements Cancellable {
	private final long userId;
	private final Fluctlight oldFluctlight;
	private boolean cancelled = false;

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}

