package me.whereareiam.yui.model.journey.definition.descriptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.journey.definition.group.JourneyStepDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable descriptor for a registered journey step.
 *
 * @param <S> journey state type
 */
@Getter
@RequiredArgsConstructor
public final class JourneyStepDescriptor<S> {
	private final @NotNull String stepId;
	private final @NotNull JourneyStepDefinition<S> definition;
	private final @Nullable String groupId;
	private final int order;
}
