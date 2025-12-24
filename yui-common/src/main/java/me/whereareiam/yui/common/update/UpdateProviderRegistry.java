package me.whereareiam.yui.common.update;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.event.update.UpdateProviderRegisteredEvent;
import me.whereareiam.yui.event.update.UpdateProviderUnregisteredEvent;
import me.whereareiam.yui.model.update.UpdateSource;
import me.whereareiam.yui.update.UpdateProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for update providers.
 * Manages the registration and retrieval of update providers.
 */
@Component
@RequiredArgsConstructor
public class UpdateProviderRegistry {
    private final Map<String, UpdateProvider> providers = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Registers a provider.
     *
     * @param provider the provider to register
     * @throws IllegalArgumentException if a provider with the same ID is already registered
     */
    public void register(UpdateProvider provider) {
        String providerId = provider.getId();

        if (providers.containsKey(providerId)) {
            throw new IllegalArgumentException("Provider with ID '" + providerId + "' is already registered");
        }

        providers.put(providerId, provider);
        eventPublisher.publishEvent(new UpdateProviderRegisteredEvent(providerId));
    }

    /**
     * Unregisters a provider by ID.
     *
     * @param providerId the provider ID
     * @return true if the provider was unregistered, false if not found
     */
    public boolean unregister(String providerId) {
        UpdateProvider removed = providers.remove(providerId);

        if (removed != null) {
            eventPublisher.publishEvent(new UpdateProviderUnregisteredEvent(providerId));
            return true;
        }

        return false;
    }

    /**
     * Gets a provider by ID.
     *
     * @param providerId the provider ID
     * @return the provider, or empty if not found
     */
    public Optional<UpdateProvider> get(String providerId) {
        return Optional.ofNullable(providers.get(providerId));
    }

    /**
     * Gets a provider by update source.
     *
     * @param source the update source
     * @return the provider, or empty if not found
     */
    public Optional<UpdateProvider> by(UpdateSource source) {
        return get(source.getProvider());
    }

    /**
     * Gets all registered providers.
     *
     * @return collection of all providers
     */
    public Collection<UpdateProvider> all() {
        return providers.values();
    }

    /**
     * Checks if a provider is registered.
     *
     * @param providerId the provider ID
     * @return true if registered, false otherwise
     */
    public boolean has(String providerId) {
        return providers.containsKey(providerId);
    }
}
