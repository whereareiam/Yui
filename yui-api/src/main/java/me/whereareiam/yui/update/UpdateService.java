package me.whereareiam.yui.update;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

/**
 * Service for managing update providers and checking for updates.
 */
@SuppressWarnings("unused")
public interface UpdateService {
    /**
     * Registers a custom update provider.
     *
     * @param provider the provider to register
     * @throws IllegalArgumentException if a provider with the same ID is already registered
     */
    void registerProvider(@NotNull UpdateProvider provider);

    /**
     * Unregisters a provider by its ID.
     *
     * @param providerId the ID of the provider to unregister
     * @return true if the provider was unregistered, false if not found
     */
    boolean unregisterProvider(@NotNull String providerId);

    /**
     * Gets a registered provider by its ID.
     *
     * @param providerId the provider ID
     * @return the provider, or empty if not found
     */
    Optional<UpdateProvider> getProvider(@NotNull String providerId);

    /**
     * Gets all registered update providers.
     *
     * @return collection of all providers
     */
    Collection<UpdateProvider> getProviders();

    /**
     * Checks for updates to the core Yui framework.
     */
    void checkUpdates();

    /**
     * Checks for updates to a specific plugin.
     *
     * @param pluginId the plugin ID
     */
    void checkPluginUpdates(@NotNull String pluginId);

    /**
     * Checks for updates to all loaded plugins.
     */
    void checkAllPluginUpdates();
}
