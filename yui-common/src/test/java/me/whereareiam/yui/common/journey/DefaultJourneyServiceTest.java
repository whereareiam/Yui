package me.whereareiam.yui.common.journey;

import me.whereareiam.yui.common.journey.engine.JourneyEngine;
import me.whereareiam.yui.event.journey.session.JourneySessionStartedEvent;
import me.whereareiam.yui.event.journey.step.JourneyStepCompletedEvent;
import me.whereareiam.yui.event.journey.step.JourneyStepEnteredEvent;
import me.whereareiam.yui.event.journey.JourneyCompletedEvent;
import me.whereareiam.yui.event.journey.JourneyFailedEvent;
import me.whereareiam.yui.common.journey.session.DefaultJourneySessionService;
import me.whereareiam.yui.common.journey.session.JourneyStoreRouting;
import me.whereareiam.yui.common.journey.session.store.DefaultJourneySessionStoreRegistry;
import me.whereareiam.yui.common.journey.session.store.InMemoryJourneySessionStore;
import me.whereareiam.yui.journey.definition.group.JourneyGroupDefinition;
import me.whereareiam.yui.journey.definition.group.JourneyStepDefinition;
import me.whereareiam.yui.journey.session.store.JourneySessionStore;
import me.whereareiam.yui.model.journey.*;
import me.whereareiam.yui.model.journey.session.JourneySession;
import me.whereareiam.yui.model.journey.session.JourneySessionRequest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultJourneyServiceTest {
	private JourneyDefinitionRegistry definitions;
	@Mock
	private ApplicationEventPublisher events;
	@Mock
	private ObjectProvider<JourneySessionStore> storeProvider;
	@Mock
	private Environment environment;
	private DefaultJourneyService service;

	@BeforeEach
	void setUp() {
		definitions = new JourneyDefinitionRegistry();
		when(storeProvider.orderedStream()).thenReturn(Stream.of(new InMemoryJourneySessionStore()));
		DefaultJourneySessionStoreRegistry stores = new DefaultJourneySessionStoreRegistry(storeProvider);
		JourneyStoreRouting storeRouting = new JourneyStoreRouting(environment);
		DefaultJourneySessionService sessionService = new DefaultJourneySessionService(
				stores,
				storeRouting,
				events
		);
		JourneyEngine engine = new JourneyEngine(
				sessionService,
				events
		);

		service = new DefaultJourneyService(
				definitions,
				sessionService,
				engine
		);
	}

	@Test
	void groupSkipSkipsAllGroupStepsAndCompletes() {
		ApplicationContext context = mock(ApplicationContext.class);
		definitions.registerGroup(context, "verification", "optional", 100, new JourneyGroupDefinition<>() {
			@Override
			public @NonNull JourneyPolicyDecision evaluatePolicy(@NonNull JourneyStepContext<Object> context) {
				return JourneyPolicyDecision.skip();
			}
		});
		definitions.registerStep(context, "verification", "welcome", 10, "", _ -> JourneyInstruction.next());
		definitions.registerStep(context, "verification", "optional-step", 20, "optional", _ -> JourneyInstruction.fail());

		JourneySession<Object> session = service.start(JourneySessionRequest.builder("verification", 1L, Object.class, new Object()).build());

		assertTrue(service.findSession(session.getId(), Object.class).isEmpty());
		verify(events).publishEvent(any(JourneyCompletedEvent.class));
		verify(events, never()).publishEvent(any(JourneyFailedEvent.class));
	}

	@Test
	void stepGuardFailPublishesFailedAndCleansSession() {
		ApplicationContext context = mock(ApplicationContext.class);
		definitions.registerStep(context, "verification", "welcome", 10, "", new JourneyStepDefinition<>() {
			@Override
			public @NonNull JourneyPolicyDecision evaluatePolicy(@NonNull JourneyStepContext<Object> context) {
				return JourneyPolicyDecision.reject();
			}

			@Override
			public @NonNull JourneyInstruction onEnter(@NonNull JourneyStepContext<Object> context) {
				return JourneyInstruction.waitForSignal();
			}
		});

		JourneySession<Object> session = service.start(JourneySessionRequest.builder("verification", 2L, Object.class, new Object()).build());

		assertTrue(service.findSession(session.getId(), Object.class).isEmpty());
		verify(events).publishEvent(any(JourneyFailedEvent.class));
	}

	@Test
	void publishesStepCompletedBeforeJourneyCompleted() {
		ApplicationContext context = mock(ApplicationContext.class);
		definitions.registerStep(context, "verification", "welcome", 10, "", _ -> JourneyInstruction.complete());

		service.start(JourneySessionRequest.builder("verification", 3L, Object.class, new Object()).build());

		var inOrder = inOrder(events);
		inOrder.verify(events).publishEvent(any(JourneySessionStartedEvent.class));
		inOrder.verify(events).publishEvent(any(JourneyStepEnteredEvent.class));
		inOrder.verify(events).publishEvent(any(JourneyStepCompletedEvent.class));
		inOrder.verify(events).publishEvent(any(JourneyCompletedEvent.class));
	}

	@Test
	void concurrentSignalsAreSerializedPerSession() throws Exception {
		AtomicInteger inFlight = new AtomicInteger();
		AtomicInteger maxInFlight = new AtomicInteger();
		ApplicationContext context = mock(ApplicationContext.class);
		definitions.registerStep(context, "verification", "welcome", 10, "", new JourneyStepDefinition<>() {
			@Override
			public @NonNull JourneyInstruction onEnter(@NonNull JourneyStepContext<Object> context) {
				return JourneyInstruction.waitForSignal();
			}

			@Override
			public @NonNull JourneyInstruction onSignal(@NonNull JourneyStepContext<Object> context, @NonNull JourneySignal signal) {
				int now = inFlight.incrementAndGet();
				maxInFlight.accumulateAndGet(now, Math::max);
				try {
					Thread.sleep(75);
				} catch (InterruptedException ignored) {
					Thread.currentThread().interrupt();
				}
				inFlight.decrementAndGet();
				return JourneyInstruction.waitForSignal();
			}
		});

		JourneySession<Object> session = service.start(JourneySessionRequest.builder("verification", 4L, Object.class, new Object()).build());
		JourneySignal signal = JourneySignal.of("go");
		CountDownLatch start = new CountDownLatch(1);
		Queue<Throwable> failures = new ConcurrentLinkedQueue<>();

		Thread t1 = new Thread(() -> {
			try {
				start.await();
				service.signal(session.getId(), Object.class, signal);
			} catch (Throwable throwable) {
				failures.add(throwable);
			}
		});
		Thread t2 = new Thread(() -> {
			try {
				start.await();
				service.signal(session.getId(), Object.class, signal);
			} catch (Throwable throwable) {
				failures.add(throwable);
			}
		});

		t1.start();
		t2.start();
		start.countDown();
		t1.join();
		t2.join();

		assertTrue(failures.isEmpty(), "signal threads failed: " + failures);
		assertEquals(1, maxInFlight.get(), "signals should run one-at-a-time for same session");
	}

}
