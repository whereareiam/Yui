package me.whereareiam.yui.common.journey.session.store;

import me.whereareiam.yui.journey.session.store.JourneySessionStore;
import me.whereareiam.yui.journey.session.store.JourneySessionStoreRegistry;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DefaultJourneySessionStoreRegistry implements JourneySessionStoreRegistry {
	private final Map<String, JourneySessionStore> stores = new ConcurrentHashMap<>();

	public DefaultJourneySessionStoreRegistry(ObjectProvider<JourneySessionStore> storeProvider) {
		storeProvider.orderedStream().forEach(this::register);
	}

	@Override
	public void register(@NonNull JourneySessionStore store) {
		if (store.getId().isBlank()) throw new IllegalArgumentException("JourneySessionStore id cannot be null or blank");

		stores.put(store.getId(), store);
	}

	@Override
	public @NonNull Optional<JourneySessionStore> get(String id) {
		if (id == null || id.isBlank())
			return Optional.empty();

		return Optional.ofNullable(stores.get(id));
	}

	@Override
	public @NonNull JourneySessionStore resolve(String id) {
		String effectiveId = (id == null || id.isBlank()) ? "in-memory" : id;
		JourneySessionStore store = stores.get(effectiveId);
		if (store == null) throw new IllegalStateException("Journey session session not found: " + effectiveId);

		return store;
	}

	@Override
	public @NonNull Collection<JourneySessionStore> all() {
		return stores.values();
	}
}
