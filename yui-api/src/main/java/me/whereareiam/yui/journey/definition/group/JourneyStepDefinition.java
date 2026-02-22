package me.whereareiam.yui.journey.definition.group;

import me.whereareiam.yui.model.journey.JourneyInstruction;
import me.whereareiam.yui.model.journey.JourneyPolicyDecision;
import me.whereareiam.yui.model.journey.JourneySignal;
import me.whereareiam.yui.model.journey.JourneyStepContext;
import org.jetbrains.annotations.NotNull;

/**
 * Contract for a journey step lifecycle.
 *
 * @param <S> journey state type
 */
public interface JourneyStepDefinition<S> {
	/**
	 * Evaluates policy before entering the step.
	 *
	 * @param context step context
	 * @return policy decision
	 */
	default @NotNull JourneyPolicyDecision evaluatePolicy(@NotNull JourneyStepContext<S> context) {
		return JourneyPolicyDecision.allow();
	}

	/**
	 * Executes when a step is entered.
	 *
	 * @param context step context
	 * @return next instruction
	 */
	@NotNull JourneyInstruction onEnter(@NotNull JourneyStepContext<S> context);

	/**
	 * Handles external signal while step waits.
	 *
	 * @param context step context
	 * @param signal incoming signal
	 * @return next instruction
	 */
	default @NotNull JourneyInstruction onSignal(
			@NotNull JourneyStepContext<S> context,
			@NotNull JourneySignal signal
	) {
		return JourneyInstruction.waitForSignal();
	}

	/**
	 * Called when journey reaches completion.
	 *
	 * @param context step context
	 */
	default void onJourneyCompleted(@NotNull JourneyStepContext<S> context) {
		// no-op
	}

	/**
	 * Called when journey is cancelled, rejected or timed out.
	 *
	 * @param context step context
	 */
	default void onJourneyCancelled(@NotNull JourneyStepContext<S> context) {
		// no-op
	}
}
