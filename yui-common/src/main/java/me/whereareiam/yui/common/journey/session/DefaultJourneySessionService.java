package me.whereareiam.yui.common.journey.session;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.event.journey.session.JourneySessionRemovedEvent;
import me.whereareiam.yui.event.journey.session.JourneySessionStartedEvent;
import me.whereareiam.yui.journey.session.JourneySessionService;
import me.whereareiam.yui.journey.session.store.JourneySessionStore;
import me.whereareiam.yui.journey.session.store.JourneySessionStoreRegistry;
import me.whereareiam.yui.model.journey.definition.JourneyDefinition;
import me.whereareiam.yui.model.journey.session.JourneySession;
import me.whereareiam.yui.model.journey.session.JourneySessionRequest;
import me.whereareiam.yui.type.journey.JourneyStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class DefaultJourneySessionService implements JourneySessionService {
	private final @NotNull JourneySessionStoreRegistry storeRegistry;
	private final @NotNull JourneyStoreRouting storeRouting;
	private final @NotNull ApplicationEventPublisher eventPublisher;
	private final @NotNull Map<String, Object> sessionLocks = new ConcurrentHashMap<>();

	@Override
	public <S> @NotNull Optional<JourneySession<S>> findSession(
			@Nullable String sessionId,
			@NotNull Class<S> stateType
	) {
		Optional<JourneySession<?>> session = findAny(sessionId);
		return session.map(journeySession -> castSession(journeySession, stateType));
	}

	@Override
	public @NotNull Optional<JourneySession<?>> findAny(@Nullable String sessionId) {
		if (sessionId == null || sessionId.isBlank())
			return Optional.empty();

		String storeId = storeRouting.getStoreId(sessionId);
		if (storeId != null) {
			Optional<JourneySession<?>> session = storeRegistry.resolve(storeId).get(sessionId);
			if (session.isPresent()) return session;

			storeRouting.unbind(sessionId);
		}

		for (JourneySessionStore store : storeRegistry.all()) {
			Optional<JourneySession<?>> session = store.get(sessionId);
			if (session.isPresent()) {
				storeRouting.bind(sessionId, store.getId());
				return session;
			}
		}

		return Optional.empty();
	}

	@Override
	public <S> @NotNull Optional<JourneySession<S>> find(
			@NotNull String journeyId,
			long participantId,
			@NotNull Set<JourneyStatus> statuses,
			@NotNull Class<S> stateType
	) {
		if (statuses.isEmpty()) return Optional.empty();

		for (JourneySessionStore store : storeRegistry.all()) {
			Optional<JourneySession<?>> found = store.find(journeyId, participantId, statuses);
			if (found.isPresent()) {
				JourneySession<?> session = found.get();
				storeRouting.bind(session.getId(), store.getId());
				return Optional.of(castSession(session, stateType));
			}
		}

		return Optional.empty();
	}

	@Override
	public @NotNull Collection<JourneySession<?>> findAll(
			@NotNull String journeyId,
			@NotNull Set<JourneyStatus> statuses
	) {
		if (statuses.isEmpty()) return java.util.List.of();

		Collection<JourneySession<?>> sessions = new ArrayList<>();
		for (JourneySessionStore store : storeRegistry.all()) {
			Collection<JourneySession<?>> storeSessions = store.findAll(journeyId, statuses);
			for (JourneySession<?> session : storeSessions)
				storeRouting.bind(session.getId(), store.getId());

			sessions.addAll(storeSessions);
		}

		return sessions;
	}

	@Override
	public <S> @NotNull JourneySession<S> create(
			@NotNull JourneySessionRequest<S> request,
			@NotNull JourneyDefinition<Object> definition
	) {
		String storeId = storeRouting.resolveStoreId(
				request.getJourneyId(),
				request.getSessionStore(),
				definition.getConfiguration()
		);

		JourneySession<S> session = new JourneySession<>(
				UUID.randomUUID().toString(),
				request.getJourneyId(),
				request.getParticipantId(),
				request.getStateType(),
				request.getState(),
				request.getAttributes(),
				storeId
		);

		save(session);
		eventPublisher.publishEvent(new JourneySessionStartedEvent(session));

		return session;
	}

	@Override
	public void save(@NotNull JourneySession<?> session) {
		JourneySessionStore store = storeRegistry.resolve(session.getRuntime().getStoreId());
		store.save(session);
		storeRouting.bind(session.getId(), store.getId());
	}

	@Override
	public void delete(@NotNull JourneySession<?> session) {
		JourneySessionStore store = storeRegistry.resolve(session.getRuntime().getStoreId());
		store.delete(session.getId());
		storeRouting.unbind(session.getId());
		releaseLock(session.getId());
		eventPublisher.publishEvent(new JourneySessionRemovedEvent(session));
	}

	@Override
	public <T> T withLock(@NotNull String sessionId, @NotNull JourneySessionService.SessionSupplier<T> supplier) {
		Object lock = sessionLocks.computeIfAbsent(sessionId, ignored -> new Object());
		synchronized (lock) {
			try {
				return supplier.get();
			} catch (RuntimeException runtime) {
				throw runtime;
			} catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		}
	}

	@Override
	public void withLock(@NotNull String sessionId, @NotNull JourneySessionService.SessionRunnable runnable) {
		withLock(sessionId, () -> {
			runnable.run();
			return null;
		});
	}

	private void releaseLock(@NotNull String sessionId) {
		sessionLocks.remove(sessionId);
	}

	@SuppressWarnings("unchecked")
	private <S> JourneySession<S> castSession(JourneySession<?> session, Class<S> stateType) {
		if (stateType == null) throw new IllegalArgumentException("State type cannot be null");

		Class<?> actual = session.getPayload().getStateType();
		if (!stateType.isAssignableFrom(actual))
			throw new IllegalStateException("Journey session state type mismatch: requested="
					+ stateType.getName() + ", actual=" + actual.getName());

		return (JourneySession<S>) session;
	}
}
