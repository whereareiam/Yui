package me.whereareiam.yui.model.journey;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.type.journey.JourneyPolicyDecisionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Result of policy evaluation for a journey step or group.
 */
@Getter
@RequiredArgsConstructor
@SuppressWarnings("unused")
public final class JourneyPolicyDecision {
	private final @NotNull JourneyPolicyDecisionType type;
	private final @Nullable String stepId;

	/**
	 * @return allow decision
	 */
	public static @NotNull JourneyPolicyDecision allow() {
		return new JourneyPolicyDecision(JourneyPolicyDecisionType.ALLOW, null);
	}

	/**
	 * @return skip decision
	 */
	public static @NotNull JourneyPolicyDecision skip() {
		return new JourneyPolicyDecision(JourneyPolicyDecisionType.SKIP, null);
	}

	/**
	 * @param stepId target step id
	 * @return redirect decision
	 */
	public static @NotNull JourneyPolicyDecision redirect(@NotNull String stepId) {
		if (stepId.isBlank()) throw new IllegalArgumentException("Step id cannot be null or blank");

		return new JourneyPolicyDecision(JourneyPolicyDecisionType.REDIRECT, stepId);
	}

	/**
	 * @return reject decision
	 */
	public static @NotNull JourneyPolicyDecision reject() {
		return new JourneyPolicyDecision(JourneyPolicyDecisionType.REJECT, null);
	}
}
