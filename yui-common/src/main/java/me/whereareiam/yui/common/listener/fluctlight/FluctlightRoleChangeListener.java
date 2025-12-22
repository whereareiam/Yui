package me.whereareiam.yui.common.listener.fluctlight;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.event.fluctlight.role.FluctlightRoleAddedEvent;
import me.whereareiam.yui.event.fluctlight.role.FluctlightRoleRemovedEvent;
import me.whereareiam.yui.fluctlight.FluctlightRegistry;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.model.fluctlight.FluctlightStateUpdater;
import me.whereareiam.yui.service.RoleService;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Listener that handles role changes and triggers role synchronization.
 * This ensures that when roles are added or removed from a Fluctlight,
 * both the in-memory state and Discord roles are synchronized.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FluctlightRoleChangeListener {
	private final RoleService roleService;
	private final FluctlightRegistry fluctlightRegistry;

	@Order(0)
	@EventListener
	public void onRoleAdded(FluctlightRoleAddedEvent event) {
		Fluctlight fluctlight = event.getFluctlight();
		long roleId = event.getRoleId();
		
		// Update in-memory state
		long[] current = fluctlight.getAllowedRoles();
		long[] updated;
		if (current == null) {
			updated = new long[]{roleId};
		} else {
			// Check if already present
			for (long role : current) {
				if (role == roleId) {
					log.debug("Role {} already present for user {}", roleId, fluctlight.getId());
					return;
				}
			}
			
			updated = new long[current.length + 1];
			System.arraycopy(current, 0, updated, 0, current.length);
			updated[current.length] = roleId;
		}
		
		FluctlightStateUpdater.updateAllowedRoles(fluctlight, updated);
		
		// Update registry
		fluctlightRegistry.putFluctlight(fluctlight.getId(), fluctlight);
		
		// Trigger role sync
		roleService.syncUserRoles(fluctlight);
		log.debug("Added role {} to user {} and triggered sync", roleId, fluctlight.getId());
	}

	@Order(0)
	@EventListener
	public void onRoleRemoved(FluctlightRoleRemovedEvent event) {
		Fluctlight fluctlight = event.getFluctlight();
		long roleId = event.getRoleId();
		
		// Update in-memory state
		long[] current = fluctlight.getAllowedRoles();
		if (current == null || current.length == 0) {
			log.debug("No roles to remove for user {}", fluctlight.getId());
			return;
		}
		
		long[] updated = new long[current.length - 1];
		int index = 0;
		for (long role : current) {
			if (role != roleId) {
				updated[index++] = role;
			}
		}
		
		FluctlightStateUpdater.updateAllowedRoles(fluctlight, updated.length > 0 ? updated : null);
		
		// Update registry
		fluctlightRegistry.putFluctlight(fluctlight.getId(), fluctlight);
		
		// Trigger role sync
		roleService.syncUserRoles(fluctlight);
		log.debug("Removed role {} from user {} and triggered sync", roleId, fluctlight.getId());
	}
}
