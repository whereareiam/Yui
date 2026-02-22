package me.whereareiam.yui.common.journey.session;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.journey.definition.JourneyConfigurationDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class JourneyStoreRouting {
	private final @NotNull Environment environment;
	private final @NotNull Map<String, String> sessionRoutes = new ConcurrentHashMap<>();

	public @NotNull String resolveStoreId(
			@NotNull String journeyId,
			@Nullable String requestStore,
			@NotNull JourneyConfigurationDefinition configuration
	) {
		if (requestStore != null && !requestStore.isBlank())
			return requestStore;

		String override = environment.getProperty("journey.store.overrides." + journeyId);
		if (override != null && !override.isBlank())
			return override;

		String configuredStore = configuration.sessionStore();
		if (configuredStore != null && !configuredStore.isBlank())
			return configuredStore;

		String globalDefault = environment.getProperty("journey.store.default");
		if (globalDefault != null && !globalDefault.isBlank())
			return globalDefault;

		return "in-memory";
	}

	public @Nullable String getStoreId(@Nullable String sessionId) {
		if (sessionId == null || sessionId.isBlank())
			return null;

		return sessionRoutes.get(sessionId);
	}

	public void bind(@NotNull String sessionId, @NotNull String storeId) {
		sessionRoutes.put(sessionId, storeId);
	}

	public void unbind(@NotNull String sessionId) {
		sessionRoutes.remove(sessionId);
	}
}
