package me.whereareiam.yui.event.fluctlight.role;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.whereareiam.yui.event.Cancellable;
import me.whereareiam.yui.model.fluctlight.Fluctlight;

/**
 * Cancellable event published BEFORE a role is added to a Fluctlight's allowed roles.
 * Listeners can cancel this event to prevent the role addition.
 */
@Getter
@Setter
@RequiredArgsConstructor
public class FluctlightRoleAddEvent implements Cancellable {
	private final Fluctlight fluctlight;
	private final long roleId;
	private boolean cancelled;
}
