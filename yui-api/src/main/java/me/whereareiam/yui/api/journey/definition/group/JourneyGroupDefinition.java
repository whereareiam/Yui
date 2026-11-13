package me.whereareiam.yui.api.journey.definition.group;

import me.whereareiam.yui.api.model.journey.JourneyPolicyDecision;
import me.whereareiam.yui.api.model.journey.JourneyStepContext;
import org.jetbrains.annotations.NotNull;

/**
 * Contract for optional group-level policy evaluation.
 *
 * @param <S> journey state type
 */
public interface JourneyGroupDefinition<S> {
	/**
	 * Evaluates policy before entering group steps.
	 *
	 * @param context step context
	 * @return policy decision
	 */
	default @NotNull JourneyPolicyDecision evaluatePolicy(@NotNull JourneyStepContext<S> context) {
		return JourneyPolicyDecision.allow();
	}
}
