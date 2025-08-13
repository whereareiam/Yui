package me.whereareiam.yui.common.listener.role;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.event.user.UserProfileClearedEvent;
import me.whereareiam.yui.api.input.UserRoleService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

/**
 * Listener that syncs user roles when their profile is cleared.
 * This ensures that Discord roles are properly synchronized after a profile clear operation.
 */
@Slf4j
@Component
@AllArgsConstructor
public class UserProfileClearedListener {
	private final UserRoleService userRoleService;
	private final ExecutorService syncPool;

	@EventListener
	public void onUserProfileCleared(UserProfileClearedEvent event) {
		long userId = event.getUserId();

		syncPool.execute(() -> userRoleService.syncUser(userId));
	}
}
