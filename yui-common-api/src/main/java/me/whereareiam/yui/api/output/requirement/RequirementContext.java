package me.whereareiam.yui.api.output.requirement;

import lombok.Getter;
import me.whereareiam.yui.api.model.profile.UserProfile;

/**
 * Context wrapper for requirement evaluation that includes UserProfile information.
 * This ensures that all requirement evaluators have access to user profile data.
 */
@Getter
public class RequirementContext {
    private final Object originalContext;
    private final UserProfile userProfile;

    public RequirementContext(Object originalContext, UserProfile userProfile) {
        this.originalContext = originalContext;
        this.userProfile = userProfile;
    }

    /**
     * Gets the original context object (e.g., SlashCommandInteractionEvent).
     * @return The original context object
     */
    public Object getOriginalContext() {
        return originalContext;
    }

    /**
     * Gets the user profile associated with this context.
     * @return The user profile, never null
     */
    public UserProfile getUserProfile() {
        return userProfile;
    }

    /**
     * Gets the user ID from the user profile.
     * @return The user ID
     */
    public long getUserId() {
        return userProfile.getId();
    }
}
