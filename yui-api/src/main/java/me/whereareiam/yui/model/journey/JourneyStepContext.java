package me.whereareiam.yui.model.journey;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.model.journey.session.JourneySession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Step execution context containing current session and optional signal payload.
 *
 * @param <S> journey state type
 */
@Getter
@RequiredArgsConstructor
public final class JourneyStepContext<S> {
	private final @NotNull JourneySession<S> session;
	private final @Nullable JourneySignal signal;

	/**
	 * Returns typed session state for convenience in step code.
	 *
	 * @return typed state
	 */
	public @NotNull S state() {
		return session.getState();
	}
}
