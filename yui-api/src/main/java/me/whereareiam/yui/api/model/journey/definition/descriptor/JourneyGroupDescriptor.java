package me.whereareiam.yui.api.model.journey.definition.descriptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.journey.definition.group.JourneyGroupDefinition;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable descriptor for a registered journey group.
 *
 * @param <S> journey state type
 */
@Getter
@RequiredArgsConstructor
public final class JourneyGroupDescriptor<S> {
	private final @NotNull String groupId;
	private final int order;
	private final @NotNull JourneyGroupDefinition<S> definition;
}
