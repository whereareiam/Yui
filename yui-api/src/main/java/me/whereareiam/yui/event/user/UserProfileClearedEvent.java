package me.whereareiam.yui.event.user;

import lombok.Getter;
import me.whereareiam.yui.event.Cancellable;
import me.whereareiam.yui.model.profile.UserProfile;

/**
 * Event published when a user's profile is cleared and reinitialized.
 * This event can be cancelled to prevent the profile clearing operation.
 */
@Getter
public class UserProfileClearedEvent implements Cancellable {
	private final long userId;
	private final UserProfile oldProfile;
	private final UserProfile newProfile;
	private boolean cancelled = false;

	public UserProfileClearedEvent(
        long userId, 
        UserProfile oldProfile, 
        UserProfile newProfile
        ) {
		this.userId = userId;
		this.oldProfile = oldProfile;
		this.newProfile = newProfile;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
