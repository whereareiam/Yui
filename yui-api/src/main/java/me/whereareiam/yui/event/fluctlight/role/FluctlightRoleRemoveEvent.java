package me.whereareiam.yui.event.fluctlight.role;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.whereareiam.yui.event.Cancellable;
import me.whereareiam.yui.model.fluctlight.Fluctlight;

/**
 * Cancellable event published BEFORE a role is removed from a Fluctlight's allowed roles.
 * Listeners can cancel this event to prevent the role removal.
 */
@Getter
@Setter
@RequiredArgsConstructor
public class FluctlightRoleRemoveEvent implements Cancellable {
	private final Fluctlight fluctlight;
	private final long roleId;
	private boolean cancelled;
}
