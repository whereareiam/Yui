package me.whereareiam.yui.common.journey;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.journey.definition.JourneyConfigurationDefinition;
import me.whereareiam.yui.journey.definition.group.JourneyGroupDefinition;
import me.whereareiam.yui.journey.definition.group.JourneyStepDefinition;
import me.whereareiam.yui.model.journey.definition.JourneyDefinition;
import me.whereareiam.yui.model.journey.definition.descriptor.JourneyGroupDescriptor;
import me.whereareiam.yui.model.journey.definition.descriptor.JourneyStepDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@SuppressWarnings("unchecked")
public class JourneyDefinitionRegistry {
	private final @NotNull Map<String, JourneyBucket> journeysById = new ConcurrentHashMap<>();
	private final @NotNull Map<ApplicationContext, ContextRegistrations> contextIndex = new ConcurrentHashMap<>();
	private final @NotNull Map<String, JourneyDefinition<Object>> definitionCache = new ConcurrentHashMap<>();

	public <S> void registerStep(
			@NotNull ApplicationContext context,
			@NotNull String journeyId,
			@NotNull String stepId,
			int order,
			@Nullable String groupId,
			@NotNull JourneyStepDefinition<S> definition
	) {
		String normalizedGroupId = groupId == null ? "" : groupId.trim();

		JourneyBucket bucket = journeysById.computeIfAbsent(journeyId, _ -> new JourneyBucket());
		StepRegistration registration = new StepRegistration(order, normalizedGroupId, (JourneyStepDefinition<Object>) definition);
		StepRegistration previous = bucket.stepsById.putIfAbsent(stepId, registration);
		if (previous != null && previous.definition() != definition)
			throw new IllegalStateException("Duplicate journey step: " + key(journeyId, stepId));

		contextIndex.computeIfAbsent(context, _ -> new ContextRegistrations())
				.registrations()
				.add(new RegistrationRef(journeyId, stepId, RegistrationType.STEP));

		invalidate(journeyId);
	}

	public <S> void registerGroup(
			@NotNull ApplicationContext context,
			@NotNull String journeyId,
			@NotNull String groupId,
			int order,
			@NotNull JourneyGroupDefinition<S> definition
	) {
		JourneyBucket bucket = journeysById.computeIfAbsent(journeyId, _ -> new JourneyBucket());
		GroupRegistration registration = new GroupRegistration(order, (JourneyGroupDefinition<Object>) definition);
		GroupRegistration previous = bucket.groupsById.putIfAbsent(groupId, registration);
		if (previous != null && previous.definition() != definition)
			throw new IllegalStateException("Duplicate journey group: " + key(journeyId, groupId));

		contextIndex.computeIfAbsent(context, _ -> new ContextRegistrations())
				.registrations()
				.add(new RegistrationRef(journeyId, groupId, RegistrationType.GROUP));
		invalidate(journeyId);
	}

	public void registerConfiguration(
			@NotNull ApplicationContext context,
			@NotNull String journeyId,
			@NotNull JourneyConfigurationDefinition definition
	) {
		JourneyBucket bucket = journeysById.computeIfAbsent(journeyId, _ -> new JourneyBucket());
		synchronized (bucket) {
			JourneyConfigurationDefinition previous = bucket.configuration();
			if (previous != null && previous != definition)
				throw new IllegalStateException("Duplicate journey configuration for journey: " + journeyId);

			if (previous == null)
				bucket.configuration(definition);
		}

		contextIndex.computeIfAbsent(context, _ -> new ContextRegistrations())
				.registrations()
				.add(new RegistrationRef(journeyId, null, RegistrationType.CONFIGURATION));
		invalidate(journeyId);
	}

	public @NotNull Optional<JourneyDefinition<Object>> get(@Nullable String journeyId) {
		if (journeyId == null || journeyId.isBlank()) return Optional.empty();

		JourneyDefinition<Object> cached = definitionCache.computeIfAbsent(journeyId, this::buildDefinition);
		if (cached.getSteps().isEmpty()) return Optional.empty();

		return Optional.of(cached);
	}

	public @NotNull Collection<JourneyDefinition<Object>> all() {
		return journeysById.keySet().stream()
				.map(this::get)
				.flatMap(Optional::stream)
				.toList();
	}

	public void unregisterByContext(@NotNull ApplicationContext context) {
		ContextRegistrations registrations = contextIndex.remove(context);
		if (registrations == null) return;

		registrations.registrations().forEach(ref -> {
			String id = ref.id();
			RegistrationType type = ref.type();
			if ((type == RegistrationType.STEP || type == RegistrationType.GROUP) && id == null)
				throw new IllegalStateException(type + " registration id cannot be null");

			switch (ref.type()) {
				case STEP -> unregisterStep(ref.journeyId(), id);
				case GROUP -> unregisterGroup(ref.journeyId(), id);
				case CONFIGURATION -> unregisterConfiguration(ref.journeyId());
			}
		});
	}

	private void unregisterStep(@NotNull String journeyId, @NotNull String stepId) {
		JourneyBucket bucket = journeysById.get(journeyId);
		if (bucket == null) return;

		StepRegistration removed = bucket.stepsById.remove(stepId);
		if (removed == null) return;

		invalidate(journeyId);
		cleanupBucketIfEmpty(journeyId, bucket);
	}

	private void unregisterGroup(@NotNull String journeyId, @NotNull String groupId) {
		JourneyBucket bucket = journeysById.get(journeyId);
		if (bucket == null) return;

		GroupRegistration removed = bucket.groupsById.remove(groupId);
		if (removed == null) return;

		invalidate(journeyId);
		cleanupBucketIfEmpty(journeyId, bucket);
	}

	private void unregisterConfiguration(@NotNull String journeyId) {
		JourneyBucket bucket = journeysById.get(journeyId);
		if (bucket == null) return;

		JourneyConfigurationDefinition removed;
		synchronized (bucket) {
			removed = bucket.configuration();
			bucket.configuration(null);
		}
		if (removed == null) return;

		invalidate(journeyId);
		cleanupBucketIfEmpty(journeyId, bucket);
	}

	private @NotNull JourneyDefinition<Object> buildDefinition(@NotNull String journeyId) {
		JourneyBucket bucket = journeysById.get(journeyId);
		if (bucket == null) {
			return new JourneyDefinition<>(journeyId, List.of(), List.of(), new JourneyConfigurationDefinition() {});
		}

		List<Map.Entry<String, StepRegistration>> steps = bucket.stepsById.entrySet().stream()
				.sorted(Comparator
						.comparingInt((Map.Entry<String, StepRegistration> entry) -> entry.getValue().order())
						.thenComparing(Map.Entry::getKey))
				.toList();

		List<Map.Entry<String, GroupRegistration>> groups = bucket.groupsById.entrySet().stream()
				.sorted(Comparator
						.comparingInt((Map.Entry<String, GroupRegistration> entry) -> entry.getValue().order())
						.thenComparing(Map.Entry::getKey))
				.toList();

		Set<String> groupIds = new HashSet<>();
		groups.forEach(group -> groupIds.add(group.getKey()));

		for (Map.Entry<String, StepRegistration> step : steps)
			if (!step.getValue().groupId().isBlank() && !groupIds.contains(step.getValue().groupId()))
				throw new IllegalStateException("Journey step references unknown group: " + key(journeyId, step.getKey()));

		JourneyConfigurationDefinition configuration = bucket.configuration() == null
				? new JourneyConfigurationDefinition() {}
				: bucket.configuration();

		List<JourneyStepDescriptor<Object>> stepDescriptors = steps.stream()
				.map(step -> new JourneyStepDescriptor<>(
						step.getKey(),
						step.getValue().definition(),
						step.getValue().groupId(),
						step.getValue().order()))
				.toList();

		List<JourneyGroupDescriptor<Object>> groupDescriptors = groups.stream()
				.map(group -> new JourneyGroupDescriptor<>(
						group.getKey(),
						group.getValue().order(),
						group.getValue().definition()))
				.toList();

		return new JourneyDefinition<>(journeyId, stepDescriptors, groupDescriptors, configuration);
	}

	private @NotNull String key(@NotNull String journeyId, @NotNull String id) {
		return journeyId + ':' + id;
	}

	private void invalidate(@NotNull String journeyId) {
		definitionCache.remove(journeyId);
		log.debug("Invalidated journey definition cache for {}", journeyId);
	}

	private void cleanupBucketIfEmpty(@NotNull String journeyId, @NotNull JourneyBucket bucket) {
		if (!bucket.stepsById.isEmpty()) return;
		if (!bucket.groupsById.isEmpty()) return;
		if (bucket.configuration() != null) return;

		journeysById.remove(journeyId, bucket);
	}

	private static final class JourneyBucket {
		private final @NotNull Map<String, StepRegistration> stepsById = new ConcurrentHashMap<>();
		private final @NotNull Map<String, GroupRegistration> groupsById = new ConcurrentHashMap<>();
		private @Nullable JourneyConfigurationDefinition configuration;

		private @Nullable JourneyConfigurationDefinition configuration() {
			return configuration;
		}

		private void configuration(@Nullable JourneyConfigurationDefinition configuration) {
			this.configuration = configuration;
		}
	}

	private static final class ContextRegistrations {
		private final @NotNull Set<RegistrationRef> registrations = ConcurrentHashMap.newKeySet();

		private @NotNull Set<RegistrationRef> registrations() {
			return registrations;
		}
	}

	private record StepRegistration(int order, @NotNull String groupId, @NotNull JourneyStepDefinition<Object> definition) {
	}

	private record GroupRegistration(int order, @NotNull JourneyGroupDefinition<Object> definition) {
	}

	private record RegistrationRef(@NotNull String journeyId, @Nullable String id, @NotNull RegistrationType type) {
	}

	private enum RegistrationType {
		STEP,
		GROUP,
		CONFIGURATION
	}
}
