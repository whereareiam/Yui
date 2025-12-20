package me.whereareiam.yui.event.fluctlight;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.model.fluctlight.Fluctlight;

/**
 * Event published when a Fluctlight is created.
 */
@Getter
@RequiredArgsConstructor
public class FluctlightCreatedEvent {
	private final Fluctlight fluctlight;
}