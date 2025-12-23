package me.whereareiam.yui.common.fluctlight;

import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.fluctlight.FluctlightRegistry;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of FluctlightRegistry.
 * <p>
 * Maintains an in-memory registry of Fluctlight instances using a ConcurrentHashMap
 * for thread-safe access.
 */
@Service
public class DefaultFluctlightRegistry implements FluctlightRegistry {
	private final Map<Long, Fluctlight> fluctlights = new ConcurrentHashMap<>();

	@Override
	public void putFluctlight(long userId, Fluctlight fluctlight) {
		fluctlights.put(userId, fluctlight);
	}

	@Override
	public Optional<Fluctlight> getFluctlight(long userId) {
		return Optional.ofNullable(fluctlights.get(userId));
	}

	@Override
	public void evictFluctlight(long userId) {
		fluctlights.remove(userId);
	}
}