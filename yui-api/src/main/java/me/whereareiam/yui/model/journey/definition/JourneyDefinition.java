package me.whereareiam.yui.model.journey.definition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.journey.definition.JourneyConfigurationDefinition;
import me.whereareiam.yui.model.journey.definition.descriptor.JourneyGroupDescriptor;
import me.whereareiam.yui.model.journey.definition.descriptor.JourneyStepDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
	 * Finds the next step id after the provided current step.
	 * Ungrouped steps are treated as single-step segments and interleaved with groups based on order.
	 *
	 * @param currentStepId current step id
	 * @return next step id, or null when there is no next step
	 */
	public @Nullable String nextStepIdAfter(@Nullable String currentStepId) {
		if (steps.isEmpty()) return null;

		List<Segment<S>> segments = orderedSegments();
		if (segments.isEmpty()) return null;

		if (currentStepId == null || currentStepId.isBlank()) {
			for (Segment<S> segment : segments) {
				String next = firstStepIdIn(segment);
				if (next != null) return next;
			}
			return null;
		}

		JourneyStepDescriptor<S> currentStep = findStep(currentStepId)
				.orElseThrow(() -> new IllegalStateException("Current step not found: " + currentStepId));

		Segment<S> currentSegment = segmentFor(segments, currentStep);
		if (currentSegment == null) return null;

		String nextInSegment = nextInSegment(currentSegment, currentStepId);
		if (nextInSegment != null) return nextInSegment;

		int segmentIndex = segments.indexOf(currentSegment);
		if (segmentIndex < 0) return null;
		for (int i = segmentIndex + 1; i < segments.size(); i++) {
			String next = firstStepIdIn(segments.get(i));
			if (next != null) return next;
		}

		return null;
	}

	private @NotNull List<Segment<S>> orderedSegments() {
		List<Segment<S>> segments = new ArrayList<>();

		for (JourneyGroupDescriptor<S> group : groups)
			segments.add(Segment.group(group));

		for (JourneyStepDescriptor<S> step : steps)
			if (step.getGroupId() == null || step.getGroupId().isBlank())
				segments.add(Segment.rootStep(step));

		segments.sort(Comparator
				.comparingInt((Segment<S> segment) -> segment.order)
				.thenComparing(segment -> segment.id));

		return segments;
	}

	private Segment<S> segmentFor(List<Segment<S>> segments, JourneyStepDescriptor<S> step) {
		if (step.getGroupId() == null || step.getGroupId().isBlank()) {
			for (Segment<S> segment : segments) {
				if (segment.groupId == null && Objects.equals(segment.stepId, step.getStepId()))
					return segment;
			}
			return null;
		}

		for (Segment<S> segment : segments) {
			if (segment.groupId != null && segment.groupId.equals(step.getGroupId()))
				return segment;
		}

		return null;
	}

	private String firstStepIdIn(Segment<S> segment) {
		if (segment == null) return null;
		if (segment.groupId == null) return segment.stepId;

		return steps.stream()
				.filter(step -> Objects.equals(segment.groupId, step.getGroupId()))
				.sorted(Comparator.comparingInt((JourneyStepDescriptor<S> step) -> step.getOrder())
						.thenComparing(JourneyStepDescriptor::getStepId))
				.map(JourneyStepDescriptor::getStepId)
				.findFirst()
				.orElse(null);
	}

	private String nextInSegment(Segment<S> segment, String currentStepId) {
		if (segment.groupId == null) return null;

		List<JourneyStepDescriptor<S>> ordered = steps.stream()
				.filter(step -> Objects.equals(segment.groupId, step.getGroupId()))
				.sorted(Comparator.comparingInt((JourneyStepDescriptor<S> step) -> step.getOrder())
						.thenComparing(JourneyStepDescriptor::getStepId))
				.toList();

		int index = -1;
		for (int i = 0; i < ordered.size(); i++) {
			if (Objects.equals(ordered.get(i).getStepId(), currentStepId)) {
				index = i;
				break;
			}
		}

		if (index < 0 || index + 1 >= ordered.size())
			return null;

		return ordered.get(index + 1).getStepId();
	}

	private static final class Segment<S> {
		private final String id;
		private final int order;
		private final String groupId;
		private final String stepId;

		private Segment(String id, int order, String groupId, String stepId) {
			this.id = id;
			this.order = order;
			this.groupId = groupId;
			this.stepId = stepId;
		}

		private static <S> Segment<S> group(JourneyGroupDescriptor<S> group) {
			return new Segment<>("group:" + group.getGroupId(), group.getOrder(), group.getGroupId(), null);
		}

		private static <S> Segment<S> rootStep(JourneyStepDescriptor<S> step) {
			return new Segment<>("step:" + step.getStepId(), step.getOrder(), null, step.getStepId());
		}
	}
}
