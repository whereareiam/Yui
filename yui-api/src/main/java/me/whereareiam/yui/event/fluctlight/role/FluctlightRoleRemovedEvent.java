package me.whereareiam.yui.event.fluctlight.role;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.model.fluctlight.Fluctlight;

/**
 * Non-cancellable event published AFTER a role is removed from a Fluctlight's allowed roles.
 * This event is published after successful persistence to the database.
 */
@Getter
@RequiredArgsConstructor
public class FluctlightRoleRemovedEvent {
	private final Fluctlight fluctlight;
	private final long roleId;
}
