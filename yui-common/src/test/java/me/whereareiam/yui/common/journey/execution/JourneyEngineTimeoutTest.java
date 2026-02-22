package me.whereareiam.yui.common.journey.execution;

import me.whereareiam.yui.common.journey.engine.JourneyEngine;
import me.whereareiam.yui.journey.definition.JourneyConfigurationDefinition;
import me.whereareiam.yui.journey.definition.group.JourneyStepDefinition;
import me.whereareiam.yui.journey.session.JourneySessionService;
import me.whereareiam.yui.model.journey.JourneyInstruction;
import me.whereareiam.yui.model.journey.JourneyStepContext;
import me.whereareiam.yui.model.journey.definition.JourneyDefinition;
import me.whereareiam.yui.model.journey.definition.descriptor.JourneyStepDescriptor;
import me.whereareiam.yui.model.journey.session.JourneySession;
import me.whereareiam.yui.type.journey.JourneyStatus;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JourneyEngineTimeoutTest {
	@Mock
	private JourneySessionService sessionService;
	@Mock
	private ApplicationEventPublisher events;

	@Test
	void cancelPublishesTerminalEvent() {
		JourneyEngine engine = new JourneyEngine(sessionService, events);

		AtomicBoolean cancelledHook = new AtomicBoolean(false);
		JourneyStepDefinition<Object> stepDefinition = new JourneyStepDefinition<>() {
			@Override
			public @NonNull JourneyInstruction onEnter(@NonNull JourneyStepContext<Object> context) {
				return JourneyInstruction.waitForSignal();
			}

			@Override
			public void onJourneyCancelled(@NonNull JourneyStepContext<Object> context) {
				cancelledHook.set(true);
			}
		};
		JourneyStepDescriptor<Object> step = new JourneyStepDescriptor<>("welcome", stepDefinition, "", 1);
		JourneyDefinition<Object> definition = new JourneyDefinition<>(
				"verification",
				List.of(step),
				List.of(),
				new JourneyConfigurationDefinition() {
				}
		);
		JourneySession<Object> session = new JourneySession<>("s1", "verification", 1L, Object.class, new Object(), null, "in-memory");
		session.getRuntime().getEnteredSteps().add("welcome");

		engine.cancel(session, definition);

		assertEquals(JourneyStatus.CANCELLED, session.getLifecycle().getStatus());
		assertTrue(cancelledHook.get(), "timeout should trigger cancellation hooks");
		verify(sessionService).save(session);
		verify(sessionService).delete(session);
		verify(events).publishEvent(any(me.whereareiam.yui.event.journey.JourneyCancelledEvent.class));
	}
}
