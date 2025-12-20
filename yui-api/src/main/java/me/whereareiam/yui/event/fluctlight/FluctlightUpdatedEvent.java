package me.whereareiam.yui.event.fluctlight;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.model.fluctlight.FluctlightData;

/**
 * Event published when Fluctlight data is updated in the database.
 * This event allows listeners to synchronize the in-memory Fluctlight object
 * with the persisted data without coupling persistence to in-memory state management.
 */
@Getter
@RequiredArgsConstructor
public class FluctlightUpdatedEvent {
	private final Fluctlight fluctlight;
	private final FluctlightData data;
}