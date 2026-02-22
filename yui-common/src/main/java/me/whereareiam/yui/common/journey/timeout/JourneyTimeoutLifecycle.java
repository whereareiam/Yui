package me.whereareiam.yui.common.journey.timeout;

import me.whereareiam.yui.common.journey.JourneyDefinitionRegistry;
import me.whereareiam.yui.common.journey.engine.JourneyEngine;
import me.whereareiam.yui.event.journey.session.JourneySessionRemovedEvent;
import me.whereareiam.yui.event.journey.session.JourneySessionStartedEvent;
import me.whereareiam.yui.event.journey.session.JourneySessionTimeoutEvent;
import me.whereareiam.yui.event.journey.JourneyCancelledEvent;
import me.whereareiam.yui.event.journey.JourneyCompletedEvent;
import me.whereareiam.yui.event.journey.JourneyFailedEvent;
import me.whereareiam.yui.journey.session.JourneySessionService;
import me.whereareiam.yui.journey.timeout.JourneyTimeoutAttributes;
import me.whereareiam.yui.model.journey.definition.JourneyDefinition;
import me.whereareiam.yui.model.journey.session.JourneySession;
import me.whereareiam.yui.type.journey.JourneyStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
public class JourneyTimeoutLifecycle {
	private final @NotNull JourneySessionService sessionService;
	private final @NotNull JourneyDefinitionRegistry definitionRegistry;
	private final @NotNull JourneyEngine engine;
	private final @NotNull TaskScheduler taskScheduler;
	private final @NotNull ApplicationEventPublisher eventPublisher;

	private final Map<String, ScheduledFuture<?>> timeoutTasks = new ConcurrentHashMap<>();

	@Autowired
	public JourneyTimeoutLifecycle(
			@NotNull JourneySessionService sessionService,
			@NotNull JourneyDefinitionRegistry definitionRegistry,
			@NotNull JourneyEngine engine,
			@NotNull ApplicationEventPublisher eventPublisher,
			@Qualifier("journeyTimeoutScheduler") @NotNull TaskScheduler taskScheduler
	) {
		this.sessionService = sessionService;
		this.definitionRegistry = definitionRegistry;
		this.engine = engine;
		this.eventPublisher = eventPublisher;
		this.taskScheduler = taskScheduler;
	}

	@EventListener
	public void onSessionStarted(@NotNull JourneySessionStartedEvent event) {
		Long timeoutSeconds = event.getSession().getPayload().getAttributes()
				.get(JourneyTimeoutAttributes.TIMEOUT_SECONDS)
				.orElse(null);
		if (timeoutSeconds == null || timeoutSeconds <= 0)
			return;

		String sessionId = event.getSession().getId();
		schedule(sessionId, timeoutSeconds);
	}

	@EventListener
	public void onSessionCompleted(@NotNull JourneyCompletedEvent event) {
		cancel(event.getSession().getId());
	}

	@EventListener
	public void onSessionCancelled(@NotNull JourneyCancelledEvent event) {
		cancel(event.getSession().getId());
	}

	@EventListener
	public void onSessionFailed(@NotNull JourneyFailedEvent event) {
		cancel(event.getSession().getId());
	}

	@EventListener
	public void onSessionRemoved(@NotNull JourneySessionRemovedEvent event) {
		cancel(event.getSession().getId());
	}

	private void schedule(@NotNull String sessionId, long timeoutSeconds) {
		cancel(sessionId);

		ScheduledFuture<?> scheduled = taskScheduler.schedule(
				() -> handleTimeout(sessionId, timeoutSeconds),
				Instant.now().plusSeconds(timeoutSeconds)
		);
		timeoutTasks.put(sessionId, scheduled);
	}

	private void handleTimeout(@NotNull String sessionId, long timeoutSeconds) {
		JourneySession<?> timedOutSession = sessionService.withLock(sessionId, () -> {
			JourneySession<?> session = sessionService.findAny(sessionId).orElse(null);
			if (session == null) return null;

			JourneyStatus status = session.getLifecycle().getStatus();
			if (status != JourneyStatus.RUNNING && status != JourneyStatus.WAITING) return null;

			JourneyDefinition<Object> definition = definitionRegistry.get(session.getJourneyId()).orElse(null);
			if (definition == null) {
				sessionService.delete(session);
				return null;
			}

			engine.cancel(session, definition);
			return session;
		});

		if (timedOutSession != null) {
			Duration elapsed = Duration.between(
					timedOutSession.getLifecycle().getStartedAt(),
					timedOutSession.getLifecycle().getUpdatedAt()
			);
			eventPublisher.publishEvent(new JourneySessionTimeoutEvent(timedOutSession, timeoutSeconds, elapsed));
		}
	}

	private void cancel(@NotNull String sessionId) {
		ScheduledFuture<?> previous = timeoutTasks.remove(sessionId);
		if (previous != null)
			previous.cancel(false);
	}

}
