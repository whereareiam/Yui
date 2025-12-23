package me.whereareiam.yui.common.listener.fluctlight;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.event.fluctlight.FluctlightClearedEvent;
import me.whereareiam.yui.service.RoleService;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Ensures Discord roles are synchronized after a Fluctlight has been cleared.
 * Clearing resets allowed roles in persistence, so we need to re-sync to drop
 * any previously assigned sync-enabled roles.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FluctlightClearListener {
	private final RoleService roleService;

	@Order(1)
	@EventListener
	public void onFluctlightCleared(FluctlightClearedEvent event) {
		roleService.syncUserRoles(event.getNewFluctlight());
		log.debug("Synced roles for user {} after clear", event.getUserId());
	}
}
