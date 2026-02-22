package me.whereareiam.yui.common.journey.timeout;

import me.whereareiam.yui.common.journey.JourneyDefinitionRegistry;
import me.whereareiam.yui.common.journey.engine.JourneyEngine;
import me.whereareiam.yui.journey.definition.JourneyConfigurationDefinition;
import me.whereareiam.yui.journey.session.JourneySessionService;
import me.whereareiam.yui.event.journey.JourneyCancelledEvent;
import me.whereareiam.yui.event.journey.JourneyCompletedEvent;
import me.whereareiam.yui.event.journey.JourneyFailedEvent;
import me.whereareiam.yui.event.journey.session.JourneySessionRemovedEvent;
import me.whereareiam.yui.event.journey.session.JourneySessionStartedEvent;
import me.whereareiam.yui.event.journey.session.JourneySessionTimeoutEvent;
import me.whereareiam.yui.model.journey.definition.JourneyDefinition;
import me.whereareiam.yui.model.journey.session.JourneyAttributes;
import me.whereareiam.yui.model.journey.session.JourneySession;
import me.whereareiam.yui.journey.JourneyKeys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JourneyTimeoutLifecycleTest {
	private JourneyTimeoutLifecycle lifecycle;
	private ScheduledExecutorService executor;
	@Mock
	private JourneySessionService sessionService;
	@Mock
	private JourneyDefinitionRegistry definitionRegistry;
	@Mock
	private JourneyEngine engine;
	@Mock
	private ApplicationEventPublisher eventPublisher;

	@AfterEach
	void tearDown() {
		if (lifecycle != null) lifecycle = null;
		if (executor != null) executor.shutdownNow();
	}

	@Test
	void schedulesTimeoutOnSessionStarted() {
		lifecycle = new JourneyTimeoutLifecycle(sessionService, definitionRegistry, engine, eventPublisher, taskScheduler());

		JourneySession<Object> session = sessionWithTimeout("s1");
		JourneyDefinition<Object> definition = new JourneyDefinition<>("verification", List.of(), List.of(), new JourneyConfigurationDefinition() {
		});

		when(definitionRegistry.get("verification")).thenReturn(Optional.of(definition));
		when(sessionService.findAny("s1")).thenReturn(Optional.of(session));
		when(sessionService.withLock(eq("s1"), any(JourneySessionService.SessionSupplier.class))).thenAnswer(invocation -> {
			JourneySessionService.SessionSupplier<?> supplier = invocation.getArgument(1);
			return supplier.get();
		});

		lifecycle.onSessionStarted(new JourneySessionStartedEvent(session));

		verify(engine, timeout(1500)).cancel(session, definition);
		verify(eventPublisher, timeout(1500)).publishEvent(any(JourneySessionTimeoutEvent.class));
	}

	@Test
	void doesNotScheduleWhenTimeoutMissing() {
		lifecycle = new JourneyTimeoutLifecycle(sessionService, definitionRegistry, engine, eventPublisher, taskScheduler());

		JourneySession<Object> session = new JourneySession<>("s2", "verification", 2L, Object.class, new Object(), null, "in-memory");

		lifecycle.onSessionStarted(new JourneySessionStartedEvent(session));

		verify(engine, after(200).never()).cancel(any(), any());
		verify(eventPublisher, after(200).never()).publishEvent(any(JourneySessionTimeoutEvent.class));
	}

	@Test
	void cancelsOnTerminalAndRemovalEvents() {
		lifecycle = new JourneyTimeoutLifecycle(sessionService, definitionRegistry, engine, eventPublisher, taskScheduler());

		JourneySession<Object> completed = sessionWithTimeout("s3");

		lifecycle.onSessionStarted(new JourneySessionStartedEvent(completed));
		lifecycle.onSessionCompleted(new JourneyCompletedEvent(completed));

		JourneySession<Object> cancelled = sessionWithTimeout("s4");
		lifecycle.onSessionStarted(new JourneySessionStartedEvent(cancelled));
		lifecycle.onSessionCancelled(new JourneyCancelledEvent(cancelled));

		JourneySession<Object> failed = sessionWithTimeout("s5");
		lifecycle.onSessionStarted(new JourneySessionStartedEvent(failed));
		lifecycle.onSessionFailed(new JourneyFailedEvent(failed));

		JourneySession<Object> removed = sessionWithTimeout("s6");
		lifecycle.onSessionStarted(new JourneySessionStartedEvent(removed));
		lifecycle.onSessionRemoved(new JourneySessionRemovedEvent(removed));

		waitForTimeoutWindow();

		verify(engine, never()).cancel(any(), any());
		verify(eventPublisher, never()).publishEvent(any(JourneySessionTimeoutEvent.class));
	}

	private static JourneySession<Object> sessionWithTimeout(String sessionId) {
		JourneyAttributes attributes = JourneyAttributes.builder()
				.put(JourneyKeys.TIMEOUT_SECONDS, 1L)
				.build();
		return new JourneySession<>(sessionId, "verification", 1L, Object.class, new Object(), attributes, "in-memory");
	}

	private static void waitForTimeoutWindow() {
		try {
			Thread.sleep(1200);
		} catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}
	}

	private TaskScheduler taskScheduler() {
		executor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "test-timeout"));
		return new ConcurrentTaskScheduler(executor);
	}
}
