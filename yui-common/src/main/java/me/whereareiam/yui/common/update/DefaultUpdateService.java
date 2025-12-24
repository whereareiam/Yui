package me.whereareiam.yui.common.update;

import me.whereareiam.yui.update.UpdateProvider;
import me.whereareiam.yui.update.UpdateService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

/**
 * Default implementation of the UpdateService.
 */
@Service
public class DefaultUpdateService implements UpdateService {
    private final UpdateProviderRegistry registry;
    private final UpdateScheduler scheduler;

    public DefaultUpdateService(UpdateProviderRegistry registry, UpdateScheduler scheduler) {
        this.registry = registry;
        this.scheduler = scheduler;
    }

    @Override
    public void registerProvider(@NotNull UpdateProvider provider) {
        registry.register(provider);
    }

    @Override
    public boolean unregisterProvider(@NotNull String providerId) {
        return registry.unregister(providerId);
    }

    @Override
    public Optional<UpdateProvider> getProvider(@NotNull String providerId) {
        return registry.get(providerId);
    }

    @Override
    public Collection<UpdateProvider> getProviders() {
        return registry.all();
    }

    @Override
    public void checkUpdates() {
        scheduler.checkCoreUpdates();
    }

    @Override
    public void checkPluginUpdates(@NotNull String pluginId) {
        scheduler.checkPluginUpdates(pluginId);
    }

    @Override
    public void checkAllPluginUpdates() {
        scheduler.checkAllPluginUpdates();
    }
}
