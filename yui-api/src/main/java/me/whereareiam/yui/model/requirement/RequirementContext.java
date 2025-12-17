package me.whereareiam.yui.model.requirement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.model.profile.UserProfile;

/**
 * Context wrapper for requirement evaluation that includes UserProfile information.
 * This ensures that all requirement evaluators have access to user profile data.
 */
@Getter
@RequiredArgsConstructor
public class RequirementContext {
	private final Object originalContext;
	private final UserProfile userProfile;

	/**
     * Gets the user ID from the user profile.
     * @return The user ID
     */
    public long getUserId() {
        return userProfile.getId();
    }
}
