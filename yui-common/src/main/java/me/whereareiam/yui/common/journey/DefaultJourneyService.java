package me.whereareiam.yui.common.journey;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.common.journey.engine.JourneyEngine;
import me.whereareiam.yui.journey.JourneyService;
import me.whereareiam.yui.journey.session.JourneySessionService;
import me.whereareiam.yui.model.journey.JourneySignal;
import me.whereareiam.yui.model.journey.definition.JourneyDefinition;
import me.whereareiam.yui.model.journey.session.JourneySession;
import me.whereareiam.yui.model.journey.session.JourneySessionRequest;
import me.whereareiam.yui.type.journey.JourneyStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DefaultJourneyService implements JourneyService {
	private final JourneyDefinitionRegistry definitionRegistry;
	private final JourneySessionService sessionService;
	private final JourneyEngine engine;

	@Override
	public <S> @NotNull JourneySession<S> start(@NotNull JourneySessionRequest<S> request) {
		JourneyDefinition<Object> definition = requireDefinition(request.getJourneyId());

		Optional<JourneySession<S>> existing = sessionService.find(
				request.getJourneyId(),
				request.getParticipantId(),
				EnumSet.of(JourneyStatus.RUNNING, JourneyStatus.WAITING),
				request.getStateType()
		);
		if (existing.isPresent()) return existing.get();

		JourneySession<S> session = sessionService.create(request, definition);
		sessionService.withLock(session.getId(), () -> engine.start(session, definition));

		return session;
	}

	@Override
	public <S> @NotNull JourneySession<S> signal(
			@NotNull String sessionId,
			@NotNull Class<S> stateType,
			@NotNull JourneySignal signal
	) {
		return sessionService.withLock(sessionId, () -> {
			JourneySession<S> current = sessionService.findSession(sessionId, stateType)
					.orElseThrow(() -> new IllegalStateException("Journey session not found: " + sessionId));

			JourneyDefinition<Object> definition = requireDefinition(current.getJourneyId());
			engine.signal(current, definition, signal);
			return current;
		});
	}

	@Override
	public <S> @NotNull Optional<JourneySession<S>> findSession(
			@Nullable String sessionId,
			@NotNull Class<S> stateType
	) {
		return sessionService.findSession(sessionId, stateType);
	}

	@Override
	public <S> @NotNull Optional<JourneySession<S>> find(
			@NotNull String journeyId,
			long participantId,
			@NotNull Set<JourneyStatus> statuses,
			@NotNull Class<S> stateType
	) {
		return sessionService.find(journeyId, participantId, statuses, stateType);
	}

	@Override
	public @NotNull Collection<JourneySession<?>> findAll(
			@NotNull String journeyId,
			@NotNull Set<JourneyStatus> statuses
	) {
		return sessionService.findAll(journeyId, statuses);
	}

	@Override
	public boolean cancel(@Nullable String sessionId) {
		if (sessionId == null || sessionId.isBlank()) return false;
		return sessionService.withLock(sessionId, () -> {
			JourneySession<?> session = sessionService.findAny(sessionId).orElse(null);
			if (session == null) return false;

			JourneyDefinition<Object> definition = definitionRegistry.get(session.getJourneyId()).orElse(null);
			if (definition == null) {
				sessionService.delete(session);
				return true;
			}

			engine.cancel(session, definition);
			return true;
		});
	}

	private JourneyDefinition<Object> requireDefinition(String journeyId) {
		return definitionRegistry.get(journeyId)
				.orElseThrow(() -> new IllegalStateException("Unknown journey: " + journeyId));
	}
}
