package me.whereareiam.yui.common.listener.role;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.event.fluctlight.FluctlightClearedEvent;
import me.whereareiam.yui.service.UserRoleService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

/**
 * Listener that syncs fluctlight roles when their Fluctlight is cleared.
 * This ensures that Discord roles are properly synchronized after a Fluctlight clear operation.
 */
@Slf4j
@Component
@AllArgsConstructor
public class UserProfileClearedListener {
	private final UserRoleService userRoleService;
	private final ExecutorService scheduledPool;

	@EventListener
	public void onFluctlightCleared(FluctlightClearedEvent event) {
		long userId = event.getUserId();

		scheduledPool.execute(() -> userRoleService.syncUser(userId));
	}
}
