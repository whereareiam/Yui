package me.whereareiam.yui.event.fluctlight;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.model.fluctlight.Fluctlight;

/**
 * Event published after a fluctlight's Fluctlight has been cleared and reinitialized.
 * This event is published for informational purposes and cannot be cancelled.
 */
@Getter
@RequiredArgsConstructor
public class FluctlightClearedEvent {
	private final long userId;
	private final Fluctlight oldFluctlight;
	private final Fluctlight newFluctlight;
}