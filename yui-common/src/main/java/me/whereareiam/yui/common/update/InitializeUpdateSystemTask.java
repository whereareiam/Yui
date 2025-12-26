package me.whereareiam.yui.common.update;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.LifecycleTask;
import me.whereareiam.yui.Registry;
import me.whereareiam.yui.common.update.provider.GitHubProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Initialization task for the update system.
 * Registers the built-in GitHub provider and prepares the update scheduler.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitializeUpdateSystemTask implements LifecycleTask {
    private final Registry<LifecycleTask> lifecycleRegistry;
    private final UpdateProviderRegistry providerRegistry;
    private final GitHubProvider gitHubProvider;
    private final UpdateScheduler updateScheduler;

    @PostConstruct
    public void registerSelf() {
        lifecycleRegistry.register(this);
    }

    @Override
    public String getName() {
        return "INIT_UPDATE_SYSTEM";
    }

    @Override
    public List<String> getDependencies() {
        // Should run after plugins are loaded so we can check plugin updates
        return List.of("INIT_PLUGINS");
    }

    @Override
    public CompletableFuture<Void> start() {
        log.debug("Initializing update system");

        // Register built-in GitHub provider
        providerRegistry.register(gitHubProvider);
        log.debug("Registered GitHub update provider");

        // Start the update scheduler
        updateScheduler.start();

        log.debug("Update system initialized");
        return CompletableFuture.completedFuture(null);
    }
}
