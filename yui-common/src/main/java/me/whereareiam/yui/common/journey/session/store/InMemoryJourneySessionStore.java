package me.whereareiam.yui.common.journey.session.store;

import me.whereareiam.yui.journey.session.store.JourneySessionStore;
import me.whereareiam.yui.model.journey.session.JourneySession;
import me.whereareiam.yui.type.journey.JourneyStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryJourneySessionStore implements JourneySessionStore {
	private final Map<String, JourneySession<?>> sessions = new ConcurrentHashMap<>();
	private final Map<String, String> journeyParticipantIndex = new ConcurrentHashMap<>();

	@Override
	public @NonNull String getId() {
		return "in-memory";
	}

	@Override
	public @NonNull Optional<JourneySession<?>> get(String sessionId) {
		return Optional.ofNullable(sessions.get(sessionId));
	}

	@Override
	public @NonNull Optional<JourneySession<?>> findActive(String journeyId, long participantId) {
		String indexKey = indexKey(journeyId, participantId);
		String sessionId = journeyParticipantIndex.get(indexKey);
		if (sessionId == null) return Optional.empty();

		JourneySession<?> session = sessions.get(sessionId);
		if (session == null) {
			journeyParticipantIndex.remove(indexKey);
			return Optional.empty();
		}

		JourneyStatus status = session.getLifecycle().getStatus();
		if (status != JourneyStatus.RUNNING && status != JourneyStatus.WAITING) {
			journeyParticipantIndex.remove(indexKey);
			return Optional.empty();
		}

		return Optional.of(session);
	}

	@Override
	public void save(@NonNull JourneySession<?> session) {
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
	public @NonNull Collection<JourneySession<?>> all() {
		return sessions.values();
	}

	private String indexKey(String journeyId, long participantId) {
		return journeyId + ':' + participantId;
	}
}
