package me.whereareiam.yui.model.requirement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.model.fluctlight.Fluctlight;

/**
 * Context wrapper for requirement evaluation that includes Fluctlight information.
 * This ensures that all requirement evaluators have access to Fluctlight data.
 */
@Getter
@RequiredArgsConstructor
public class RequirementContext {
	private final Object originalContext;
	private final Fluctlight fluctlight;

	/**
     * Gets the fluctlight ID from the Fluctlight.
     * @return The fluctlight ID
     */
    public long getUserId() {
        return fluctlight.getId();
    }
}
