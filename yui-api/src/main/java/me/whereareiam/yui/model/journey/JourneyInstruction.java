package me.whereareiam.yui.model.journey;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.type.journey.JourneyInstructionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Instruction returned by step lifecycle methods to control journey execution.
 */
@Getter
@RequiredArgsConstructor
@SuppressWarnings("unused")
public final class JourneyInstruction {
	private final @NotNull JourneyInstructionType type;
	private final @Nullable String stepId;

	/**
	 * @return wait instruction
	 */
	public static @NotNull JourneyInstruction waitForSignal() {
		return new JourneyInstruction(JourneyInstructionType.WAIT, null);
	}

	/**
	 * @return next-step instruction
	 */
	public static @NotNull JourneyInstruction next() {
		return new JourneyInstruction(JourneyInstructionType.NEXT, null);
	}

	/**
	 * @param stepId target step id
	 * @return redirect instruction
	 */
	public static @NotNull JourneyInstruction goTo(@NotNull String stepId) {
		if (stepId.isBlank()) throw new IllegalArgumentException("Step id cannot be null or blank");

		return new JourneyInstruction(JourneyInstructionType.GOTO, stepId);
	}

	/**
	 * @return complete instruction
	 */
	public static @NotNull JourneyInstruction complete() {
		return new JourneyInstruction(JourneyInstructionType.COMPLETE, null);
	}

	/**
	 * @return cancel instruction
	 */
	public static @NotNull JourneyInstruction cancel() {
		return new JourneyInstruction(JourneyInstructionType.CANCEL, null);
	}

	/**
	 * @return fail instruction
	 */
	public static @NotNull JourneyInstruction fail() {
		return new JourneyInstruction(JourneyInstructionType.FAIL, null);
	}

	/**
	 * @return ignore instruction
	 */
	public static @NotNull JourneyInstruction ignore() {
		return new JourneyInstruction(JourneyInstructionType.IGNORE, null);
	}
}
