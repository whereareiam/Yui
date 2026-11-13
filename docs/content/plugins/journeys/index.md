---
title: Journeys and onboarding steps
description: Add a step to Onboarding's journey, shown on the one message every step edits.
---

A journey is a sequence of steps one member goes through, such as onboarding. Each step shows something, waits for the member and tells the journey what comes next. A plugin adds a step to another plugin's journey by naming it; the step goes when the plugin stops.

Interests' topics step, shortened:

```java
@Component
@RequiredArgsConstructor
@JourneyStep(journeyId = "onboarding", stepId = "topics", order = 30)
public class TopicsStep implements JourneyStepDefinition<Object> {
	private final OnboardingService onboarding;
	private final JourneyService journeys;
	private final InteractionService interactions;

	@Override
	public @NotNull JourneyPolicyDecision evaluatePolicy(@NotNull JourneyStepContext<Object> context) {
		return topics.isEmpty() ? JourneyPolicyDecision.skip() : JourneyPolicyDecision.allow();
	}

	@Override
	public @NotNull JourneyInstruction onEnter(@NotNull JourneyStepContext<Object> context) {
		long member = context.getSession().getParticipantId();
		ComponentAttributes attributes = JourneyKeys.forSession(context.getSession().getId());
		Button done = interactions.createButton(ButtonStyle.SUCCESS, "topics-done", label, attributes);
		onboarding.show(member, embed, List.of(ActionRow.of(menu), ActionRow.of(done)));

		return JourneyInstruction.waitForSignal();
	}

	@Override
	public @NotNull JourneyInstruction onSignal(@NotNull JourneyStepContext<Object> context, @NotNull JourneySignal signal) {
		return "topics-done".equals(signal.getType()) ? JourneyInstruction.next() : JourneyInstruction.waitForSignal();
	}

	@ComponentListener("topics-done")
	public void onDone(ButtonInteractionEvent event) {
		event.deferEdit().queue();
		String session = interactions.getAttributes(event).get(JourneyKeys.SESSION_ID).orElse(null);
		journeys.findSession(session, Object.class)
				.filter(found -> found.getParticipantId() == event.getUser().getIdLong())
				.ifPresent(found -> journeys.signal(found.getId(), Object.class, JourneySignal.of("topics-done")));
	}
}
```

- `order` places the step: Onboarding's welcome is 10, languages 20, end 999; Interests uses 30, Arcadeya's products 40.
- `evaluatePolicy` skips the step when it has nothing to ask.
- `onEnter` shows the step and returns `waitForSignal()`; a click sends a signal, and `onSignal` answers `next()` to go on.
- `OnboardingService.show` puts the step on the onboarding message, the one message every step edits, so the member reads one message that changes rather than a pile of them.
- Check that the click comes from the member the journey is for: staff see the conversation too.

The plugin needs Onboarding: `dependencies.add("onboarding")` in `yuiPlugin { }`, and `compileOnly` of `me.whereareiam.yui:onboarding-api`. A journey's state stays in memory: a restart ends every journey that runs, and Onboarding starts them again when its `startup` trigger is on.
