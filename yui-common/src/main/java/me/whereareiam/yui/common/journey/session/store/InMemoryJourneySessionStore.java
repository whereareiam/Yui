package me.whereareiam.yui.common.journey.session.store;

import me.whereareiam.yui.journey.session.store.JourneySessionStore;
import me.whereareiam.yui.model.journey.session.JourneySession;
import me.whereareiam.yui.type.journey.JourneyStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryJourneySessionStore implements JourneySessionStore {
	private final Map<String, JourneySession<?>> sessions = new ConcurrentHashMap<>();
	private final Map<String, String> journeyParticipantIndex = new ConcurrentHashMap<>();

	@Override
	public @NotNull String getId() {
		return "in-memory";
	}

	@Override
	public @NotNull Optional<JourneySession<?>> get(String sessionId) {
		return Optional.ofNullable(sessions.get(sessionId));
	}

	@Override
	public @NotNull Optional<JourneySession<?>> find(
			@NotNull String journeyId,
			long participantId,
			@NotNull Set<JourneyStatus> statuses
	) {
		if (statuses.isEmpty()) return Optional.empty();

		String indexKey = indexKey(journeyId, participantId);
		String sessionId = journeyParticipantIndex.get(indexKey);
		if (sessionId == null) return Optional.empty();

		JourneySession<?> session = sessions.get(sessionId);
		if (session == null) {
			journeyParticipantIndex.remove(indexKey);
			return Optional.empty();
		}

		JourneyStatus status = session.getLifecycle().getStatus();
		if (!statuses.contains(status)) return Optional.empty();

		return Optional.of(session);
	}

	@Override
	public @NotNull Collection<JourneySession<?>> findAll(
			@NotNull String journeyId,
			@NotNull Set<JourneyStatus> statuses
	) {
		if (statuses.isEmpty()) return java.util.List.of();

		Collection<JourneySession<?>> found = new ArrayList<>();
		for (JourneySession<?> session : sessions.values()) {
			if (!journeyId.equals(session.getJourneyId())) continue;
			if (!statuses.contains(session.getLifecycle().getStatus())) continue;
			found.add(session);
		}

		return found;
	}

	@Override
	public void save(@NotNull JourneySession<?> session) {
		sessions.put(session.getId(), session);
		journeyParticipantIndex.put(indexKey(session.getJourneyId(), session.getParticipantId()), session.getId());
	}

	@Override
	public void delete(String sessionId) {
		JourneySession<?> removed = sessions.remove(sessionId);
		if (removed != null)
			journeyParticipantIndex.remove(indexKey(removed.getJourneyId(), removed.getParticipantId()));
	}

	@Override
	public @NotNull Collection<JourneySession<?>> all() {
		return sessions.values();
	}

	private String indexKey(String journeyId, long participantId) {
		return journeyId + ':' + participantId;
	}
}
