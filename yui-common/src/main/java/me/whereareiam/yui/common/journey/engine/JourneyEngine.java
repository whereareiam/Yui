package me.whereareiam.yui.common.journey.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.event.journey.JourneyCancelledEvent;
import me.whereareiam.yui.event.journey.JourneyCompletedEvent;
import me.whereareiam.yui.event.journey.JourneyFailedEvent;
import me.whereareiam.yui.event.journey.step.JourneyStepCompletedEvent;
import me.whereareiam.yui.event.journey.step.JourneyStepEnteredEvent;
import me.whereareiam.yui.event.journey.step.JourneyStepWaitingEvent;
import me.whereareiam.yui.journey.session.JourneySessionService;
import me.whereareiam.yui.model.journey.JourneyInstruction;
import me.whereareiam.yui.model.journey.JourneyPolicyDecision;
import me.whereareiam.yui.model.journey.JourneySignal;
import me.whereareiam.yui.model.journey.JourneyStepContext;
import me.whereareiam.yui.model.journey.definition.JourneyDefinition;
import me.whereareiam.yui.model.journey.definition.descriptor.JourneyGroupDescriptor;
import me.whereareiam.yui.model.journey.definition.descriptor.JourneyStepDescriptor;
import me.whereareiam.yui.model.journey.session.JourneySession;
import me.whereareiam.yui.type.journey.JourneyPolicyDecisionType;
import me.whereareiam.yui.type.journey.JourneyStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JourneyEngine {
	private static final int MAX_TRANSITION_DEPTH = 256;

	private final @NotNull JourneySessionService sessionService;
	private final @NotNull ApplicationEventPublisher eventPublisher;

	public void start(@NotNull JourneySession<?> session, @NotNull JourneyDefinition<Object> definition) {
		run(session, definition, null, JourneyInstruction.next());
	}

	public @NotNull JourneySession<?> signal(
			@NotNull JourneySession<?> session,
			@NotNull JourneyDefinition<Object> definition,
			@NotNull JourneySignal signal
	) {
		JourneyStatus status = session.getLifecycle().getStatus();
		if (status != JourneyStatus.WAITING && status != JourneyStatus.RUNNING)
			return session;

		String currentStepId = session.getLifecycle().getCurrentStepId();
		JourneyStepDescriptor<Object> step = definition.findStep(currentStepId)
				.orElseThrow(() -> new IllegalStateException("Current step not found: " + currentStepId));

		JourneyStepContext<Object> context = new JourneyStepContext<>(asEngineSession(session), signal);
		JourneyInstruction instruction = step.getDefinition().onSignal(context, signal);
		run(session, definition, currentStepId, instruction);

		return session;
	}

	public void cancel(@NotNull JourneySession<?> session, @NotNull JourneyDefinition<Object> definition) {
		session.setStatus(JourneyStatus.CANCELLED);
		session.setCurrentStepId(null);

		sessionService.save(session);
		runCancellationHooks(session, definition);
		eventPublisher.publishEvent(new JourneyCancelledEvent(session));
		sessionService.delete(session);
	}

	private void run(
			@NotNull JourneySession<?> session,
			@NotNull JourneyDefinition<Object> definition,
			@Nullable String currentStepId,
			@Nullable JourneyInstruction instruction
	) {
		JourneyInstruction nextInstruction = instruction == null ? JourneyInstruction.waitForSignal() : instruction;
		String stepId = currentStepId;
		int currentDepth = 0;

		while (true) {
			if (currentDepth > MAX_TRANSITION_DEPTH) {
				fail(session, definition);
				return;
			}

			switch (nextInstruction.getType()) {
				case WAIT, IGNORE -> {
					session.setStatus(JourneyStatus.WAITING);
					sessionService.save(session);
					publishStepWaiting(session, stepId);
					return;
				}
				case NEXT -> {
					publishStepCompleted(session, stepId);
					String nextStepId = definition.nextStepIdAfter(stepId);
					if (nextStepId == null) {
						complete(session, definition);
						return;
					}

					StepEnterResult entered = enterStep(session, definition, nextStepId, currentDepth + 1);
					if (entered == null)
						return;

					stepId = entered.stepId();
					nextInstruction = entered.instruction();
					currentDepth = entered.depth();
				}
				case GOTO -> {
					publishStepCompleted(session, stepId);
					String target = nextInstruction.getStepId();
					if (isNotValidStep(definition, target)) {
						fail(session, definition);
						return;
					}

					StepEnterResult entered = enterStep(session, definition, target, currentDepth + 1);
					if (entered == null)
						return;

					stepId = entered.stepId();
					nextInstruction = entered.instruction();
					currentDepth = entered.depth();
				}
				case COMPLETE -> {
					publishStepCompleted(session, stepId);
					complete(session, definition);
					return;
				}
				case CANCEL -> {
					cancel(session, definition);
					return;
				}
				case FAIL -> {
					fail(session, definition);
					return;
				}
			}
		}
	}

	private StepEnterResult enterStep(
			@NotNull JourneySession<?> session,
			@NotNull JourneyDefinition<Object> definition,
			@NotNull String stepId,
			int depth
	) {
		String currentStepId = stepId;
		int currentDepth = depth;

		while (true) {
			if (currentDepth > MAX_TRANSITION_DEPTH) {
				fail(session, definition);
				return null;
			}

			String finalCurrentStepId = currentStepId;
			JourneyStepDescriptor<Object> step = definition.findStep(currentStepId)
					.orElseThrow(() -> new IllegalStateException("Unknown step id: " + finalCurrentStepId));

			JourneyPolicyDecision groupDecision = groupDecision(session, definition, step.getGroupId());
			switch (groupDecision.getType()) {
				case SKIP -> {
					String nextStepId = definition.nextStepIdAfter(currentStepId);
					if (nextStepId == null) {
						complete(session, definition);
						return null;
					}

					currentStepId = nextStepId;
					currentDepth++;
					continue;
				}
				case REDIRECT -> {
					String redirect = groupDecision.getStepId();
					if (isNotValidStep(definition, redirect)) {
						fail(session, definition);
						return null;
					}

					currentStepId = redirect;
					currentDepth++;
					continue;
				}
				case REJECT -> {
					fail(session, definition);
					return null;
				}
				default -> {
				}
			}

			JourneyStepContext<Object> context = new JourneyStepContext<>(asEngineSession(session), null);
			JourneyPolicyDecision stepDecision = step.getDefinition().evaluatePolicy(context);
			switch (stepDecision.getType()) {
				case SKIP -> {
					String nextStepId = definition.nextStepIdAfter(currentStepId);
					if (nextStepId == null) {
						complete(session, definition);
						return null;
					}

					currentStepId = nextStepId;
					currentDepth++;
					continue;
				}
				case REDIRECT -> {
					String redirect = stepDecision.getStepId();
					if (isNotValidStep(definition, redirect)) {
						fail(session, definition);
						return null;
					}

					currentStepId = redirect;
					currentDepth++;
					continue;
				}
				case REJECT -> {
					fail(session, definition);
					return null;
				}
				default -> {
				}
			}

			session.setCurrentStepId(currentStepId);
			session.setStatus(JourneyStatus.RUNNING);
			session.getRuntime().getEnteredSteps().add(currentStepId);
			sessionService.save(session);
			eventPublisher.publishEvent(new JourneyStepEnteredEvent(session, currentStepId));

			JourneyInstruction instruction = step.getDefinition().onEnter(context);
			return new StepEnterResult(currentStepId, instruction, currentDepth);
		}
	}

	private JourneyPolicyDecision groupDecision(
			@NotNull JourneySession<?> session,
			@NotNull JourneyDefinition<Object> definition,
			@Nullable String groupId
	) {
		if (groupId == null || groupId.isBlank()) return JourneyPolicyDecision.allow();

		JourneyPolicyDecisionType cached = session.getRuntime().getGroupDecisions().get(groupId);
		if (cached == JourneyPolicyDecisionType.ALLOW) return JourneyPolicyDecision.allow();
		if (cached == JourneyPolicyDecisionType.SKIP) return JourneyPolicyDecision.skip();

		JourneyGroupDescriptor<Object> group = definition.findGroup(groupId)
				.orElseThrow(() -> new IllegalStateException("Unknown group id: " + groupId));
		JourneyPolicyDecision decision = group.getDefinition().evaluatePolicy(new JourneyStepContext<>(asEngineSession(session), null));
		if (decision.getType() == JourneyPolicyDecisionType.ALLOW || decision.getType() == JourneyPolicyDecisionType.SKIP)
			session.getRuntime().getGroupDecisions().put(groupId, decision.getType());

		return decision;
	}

	private void complete(@NotNull JourneySession<?> session, @NotNull JourneyDefinition<Object> definition) {
		session.setStatus(JourneyStatus.COMPLETED);
		session.setCurrentStepId(null);

		sessionService.save(session);
		runCompletionHooks(session, definition);
		eventPublisher.publishEvent(new JourneyCompletedEvent(session));
		sessionService.delete(session);
	}

	private void fail(@NotNull JourneySession<?> session, @NotNull JourneyDefinition<Object> definition) {
		session.setStatus(JourneyStatus.FAILED);
		session.setCurrentStepId(null);

		sessionService.save(session);
		runCancellationHooks(session, definition);
		eventPublisher.publishEvent(new JourneyFailedEvent(session));
		sessionService.delete(session);
	}

	private void publishStepCompleted(@NotNull JourneySession<?> session, @Nullable String stepId) {
		if (stepId == null || stepId.isBlank()) return;
		eventPublisher.publishEvent(new JourneyStepCompletedEvent(session, stepId));
	}

	private void publishStepWaiting(@NotNull JourneySession<?> session, @Nullable String stepId) {
		if (stepId == null || stepId.isBlank()) return;
		eventPublisher.publishEvent(new JourneyStepWaitingEvent(session, stepId));
	}

	private void runCompletionHooks(@NotNull JourneySession<?> session, @NotNull JourneyDefinition<Object> definition) {
		JourneyStepContext<Object> context = new JourneyStepContext<>(asEngineSession(session), null);
		for (JourneyStepDescriptor<Object> step : definition.getSteps()) {
			if (!session.getRuntime().getEnteredSteps().contains(step.getStepId()))
				continue;

			try {
				step.getDefinition().onJourneyCompleted(context);
			} catch (Exception ex) {
				log.warn("Journey completion hook failed for step {}", step.getStepId(), ex);
			}
		}
	}

	private void runCancellationHooks(@NotNull JourneySession<?> session, @NotNull JourneyDefinition<Object> definition) {
		JourneyStepContext<Object> context = new JourneyStepContext<>(asEngineSession(session), null);
		for (JourneyStepDescriptor<Object> step : definition.getSteps()) {
			if (!session.getRuntime().getEnteredSteps().contains(step.getStepId()))
				continue;

			try {
				step.getDefinition().onJourneyCancelled(context);
			} catch (Exception ex) {
				log.warn("Journey cancellation hook failed for step {}", step.getStepId(), ex);
			}
		}
	}

	private boolean isNotValidStep(@NotNull JourneyDefinition<Object> definition, @Nullable String stepId) {
		return stepId == null || stepId.isBlank() || definition.findStep(stepId).isEmpty();
	}

	@SuppressWarnings("unchecked")
	private JourneySession<Object> asEngineSession(@NotNull JourneySession<?> session) {
		return (JourneySession<Object>) session;
	}

	private record StepEnterResult(String stepId, JourneyInstruction instruction, int depth) {
	}
}
