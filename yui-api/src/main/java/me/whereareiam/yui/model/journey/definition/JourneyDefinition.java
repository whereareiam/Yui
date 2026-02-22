package me.whereareiam.yui.model.journey.definition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.journey.definition.JourneyConfigurationDefinition;
import me.whereareiam.yui.model.journey.definition.descriptor.JourneyGroupDescriptor;
import me.whereareiam.yui.model.journey.definition.descriptor.JourneyStepDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable journey definition assembled from registered step, group and configuration components.
 *
 * @param <S> journey state type
 */
@Getter
@RequiredArgsConstructor
public final class JourneyDefinition<S> {
	private final @NotNull String journeyId;
	private final @NotNull List<JourneyStepDescriptor<S>> steps;
	private final @NotNull List<JourneyGroupDescriptor<S>> groups;
	private final @NotNull JourneyConfigurationDefinition configuration;

	/**
	 * Finds a step descriptor by id.
	 *
	 * @param stepId step identifier
	 * @return optional step descriptor
	 */
	public @NotNull Optional<JourneyStepDescriptor<S>> findStep(@Nullable String stepId) {
		if (stepId == null) return Optional.empty();

		return steps.stream()
				.filter(step -> Objects.equals(step.getStepId(), stepId))
				.findFirst();
	}

	/**
	 * Finds a group descriptor by id.
	 *
	 * @param groupId group identifier
	 * @return optional group descriptor
	 */
	public @NotNull Optional<JourneyGroupDescriptor<S>> findGroup(@Nullable String groupId) {
		if (groupId == null) return Optional.empty();

		return groups.stream()
				.filter(group -> Objects.equals(group.getGroupId(), groupId))
				.findFirst();
	}

	/**
	 * Finds the next group id after the provided group id.
	 * Root steps are represented by {@code null} group id and are not part of group traversal list.
	 *
	 * @param currentGroupId current group id, {@code null} for root
	 * @return next group id, or null when there is no next group
	 */
	public @Nullable String nextGroupIdAfter(@Nullable String currentGroupId) {
		List<JourneyGroupDescriptor<S>> sortedGroups = sortedGroups();

		if (sortedGroups.isEmpty()) return null;
		if (currentGroupId == null || currentGroupId.isBlank())
			return sortedGroups.getFirst().getGroupId();

		int currentIndex = -1;
		for (int i = 0; i < sortedGroups.size(); i++) {
			if (Objects.equals(sortedGroups.get(i).getGroupId(), currentGroupId)) {
				currentIndex = i;
				break;
			}
		}

		if (currentIndex < 0 || currentIndex + 1 >= sortedGroups.size())
			return null;

		return sortedGroups.get(currentIndex + 1).getGroupId();
	}

	/**
	 * Finds the next step id inside the provided group.
	 *
	 * @param groupId target group id, {@code null} for root steps
	 * @param currentStepId current step id within the group, or {@code null} to get first step in group
	 * @return next step id, or null when there is no next step
	 */
	public @Nullable String nextStepIdAfter(@Nullable String groupId, @Nullable String currentStepId) {
		List<JourneyStepDescriptor<S>> ordered = orderedStepsForGroup(groupId);
		if (ordered.isEmpty() && (groupId == null || groupId.isBlank()) && currentStepId == null) {
			String nextGroupId = nextGroupIdAfter(null);
			while (nextGroupId != null) {
				List<JourneyStepDescriptor<S>> nextGroupSteps = orderedStepsForGroup(nextGroupId);
				if (!nextGroupSteps.isEmpty()) return nextGroupSteps.getFirst().getStepId();

				nextGroupId = nextGroupIdAfter(nextGroupId);
			}
			return null;
		}

		if (ordered.isEmpty()) return null;
		if (currentStepId == null || currentStepId.isBlank())
			return ordered.getFirst().getStepId();

		int currentIndex = -1;
		for (int i = 0; i < ordered.size(); i++) {
			if (Objects.equals(ordered.get(i).getStepId(), currentStepId)) {
				currentIndex = i;
				break;
			}
		}

		if (currentIndex < 0 || currentIndex + 1 >= ordered.size())
			return null;

		return ordered.get(currentIndex + 1).getStepId();
	}

	/**
	 * Finds the next step id after the provided current step across groups.
	 *
	 * @param currentStepId current step id
	 * @return next step id, or null when there is no next step
	 */
	public @Nullable String nextStepIdAfter(@Nullable String currentStepId) {
		if (currentStepId == null || currentStepId.isBlank())
			return nextStepIdAfter(null, null);

		JourneyStepDescriptor<S> currentStep = findStep(currentStepId)
				.orElseThrow(() -> new IllegalStateException("Current step not found: " + currentStepId));

		String currentGroupId = currentStep.getGroupId();
		String nextInGroup = nextStepIdAfter(currentGroupId, currentStepId);
		if (nextInGroup != null) return nextInGroup;

		String nextGroupId = nextGroupIdAfter(currentGroupId);
		while (nextGroupId != null) {
			String firstInNextGroup = nextStepIdAfter(nextGroupId, null);
			if (firstInNextGroup != null) return firstInNextGroup;

			nextGroupId = nextGroupIdAfter(nextGroupId);
		}

		return null;
	}

	private @NotNull List<JourneyGroupDescriptor<S>> sortedGroups() {
		return groups.stream()
				.sorted(Comparator.comparingInt((JourneyGroupDescriptor<S> group) -> group.getOrder())
						.thenComparing(JourneyGroupDescriptor::getGroupId))
				.toList();
	}

	private @NotNull List<JourneyStepDescriptor<S>> orderedStepsForGroup(@Nullable String groupId) {
		if (groupId == null || groupId.isBlank()) {
			return steps.stream()
					.filter(step -> step.getGroupId() == null || step.getGroupId().isBlank())
					.sorted(Comparator.comparingInt((JourneyStepDescriptor<S> step) -> step.getOrder())
							.thenComparing(JourneyStepDescriptor::getStepId))
					.toList();
		}

		return steps.stream()
				.filter(step -> Objects.equals(groupId, step.getGroupId()))
				.sorted(Comparator.comparingInt((JourneyStepDescriptor<S> step) -> step.getOrder())
						.thenComparing(JourneyStepDescriptor::getStepId))
				.toList();
	}
}
