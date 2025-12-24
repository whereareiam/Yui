package me.whereareiam.yui.common.update;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.BuildConfig;
import me.whereareiam.yui.Registry;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yui.event.update.UpdateAvailableEvent;
import me.whereareiam.yui.event.update.UpdateCheckFailedEvent;
import me.whereareiam.yui.event.update.UpdateLocalNotificationEvent;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.config.settings.UpdaterSettings;
import me.whereareiam.yui.model.plugin.InternalPlugin;
import me.whereareiam.yui.model.plugin.Plugin;
import me.whereareiam.yui.model.update.UpdateConfiguration;
import me.whereareiam.yui.model.update.UpdateSource;
import me.whereareiam.yui.plugin.PluginManager;
import me.whereareiam.yui.update.UpdateProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

/**
 * Scheduler for checking updates to the core framework and plugins.
 * Implements Reloadable to reschedule when settings change.
 */
@Slf4j
@Component
public class UpdateScheduler implements Reloadable {
    private static final int BRANCH_UPDATE_LIMIT = 50;
    private static final String CORE_COMPONENT_ID = "yui";

    /**
     * Core framework: dev via GitHub (for testing with commits)
     */
    private static final UpdateConfiguration CORE_SPEC = new UpdateConfiguration(
            new UpdateSource("github", "whereareiam/Yui"), // release
            new UpdateSource("github", "whereareiam/Yui")  // dev
    );

    private final ObjectProvider<Settings> settingsProvider;
    private final UpdateProviderRegistry providerRegistry;
    private final PluginManager pluginManager;
    private final ApplicationEventPublisher eventPublisher;
    private final Registry<Reloadable> reloadableRegistry;
    private final ThreadPoolTaskScheduler taskScheduler;

    private ScheduledFuture<?> scheduledTask;

    public UpdateScheduler(
            ObjectProvider<Settings> settingsProvider,
            UpdateProviderRegistry providerRegistry,
            PluginManager pluginManager,
            ApplicationEventPublisher eventPublisher,
            Registry<Reloadable> reloadableRegistry
    ) {
        this.settingsProvider = settingsProvider;
        this.providerRegistry = providerRegistry;
        this.pluginManager = pluginManager;
        this.eventPublisher = eventPublisher;
        this.reloadableRegistry = reloadableRegistry;

        // Initialize ThreadPoolTaskScheduler
        this.taskScheduler = new ThreadPoolTaskScheduler();
        this.taskScheduler.setPoolSize(1);
        this.taskScheduler.setThreadNamePrefix("update-scheduler-");
        this.taskScheduler.initialize();
    }

    @PostConstruct
    public void registerSelf() {
        reloadableRegistry.register(this);
    }

    /**
     * Starts the update scheduler based on settings configuration.
     */
    public void start() {
        Settings settings = settingsProvider.getObject();
        UpdaterSettings cfg = settings.getUpdater();

        if (!cfg.isCheckForUpdates() || cfg.getInterval().getSeconds() <= 0) {
            log.debug("[Updater] Update checking is disabled");
            return;
        }

        Duration interval = Duration.ofSeconds(cfg.getInterval().getSeconds());
        Instant startTime = Instant.now();

        // Schedule periodic check
        scheduledTask = taskScheduler.scheduleWithFixedDelay(
                this::scheduledCheck,
                startTime,
                interval
        );

        log.debug("[Updater] Update scheduler started with interval: {}", cfg.getInterval());
    }

    /**
     * Stops the update scheduler.
     */
    public void stop() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(false);
            log.debug("[Updater] Update scheduler stopped");
        }
    }

    /**
     * Reloads the update scheduler with new settings.
     * Stops the current schedule and starts a new one with updated interval.
     */
    @Override
    public void reload() {
        log.debug("[Updater] Reloading update scheduler");
        stop();
        start();
    }

    /**
     * Scheduled update check.
     */
    private void scheduledCheck() {
        Settings settings = settingsProvider.getObject();
        UpdaterSettings cfg = settings.getUpdater();

        if (cfg == null || !cfg.isCheckForUpdates())
            return;

        checkCoreUpdates();

        if (cfg.isCheckPluginUpdates()) {
            checkAllPluginUpdates();
        }
    }

    /**
     * Checks for core framework updates.
     */
    public void checkCoreUpdates() {
        Settings settings = settingsProvider.getObject();
        UpdaterSettings cfg = settings.getUpdater();
        if (cfg == null) return;

        checkEntry(CORE_COMPONENT_ID, "Yui", BuildConfig.VERSION, CORE_SPEC, cfg);
    }

    /**
     * Checks for updates to a specific plugin.
     *
     * @param pluginId the plugin ID
     */
    public void checkPluginUpdates(String pluginId) {
        Settings settings = settingsProvider.getObject();
        UpdaterSettings cfg = settings.getUpdater();
        if (cfg == null) return;

        Optional<InternalPlugin> pluginOpt = pluginManager.plugins().stream()
                .filter(p -> p.getDescriptor().getId().equals(pluginId))
                .findFirst();

        if (pluginOpt.isEmpty()) {
            log.warn("[Updater] Plugin '{}' not found", pluginId);
            return;
        }

        InternalPlugin plugin = pluginOpt.get();
        Plugin descriptor = plugin.getDescriptor();
        UpdateConfiguration updateConfig = descriptor.getUpdater();

        if (updateConfig == null) {
            log.debug("[Updater] Plugin '{}' has no update configuration", pluginId);
            return;
        }

        checkEntry(
                "plugin_" + pluginId,
                descriptor.getName(),
                descriptor.getVersion(),
                updateConfig,
                cfg
        );
    }

    /**
     * Checks for updates to all loaded plugins.
     */
    public void checkAllPluginUpdates() {
        pluginManager.plugins().stream()
                .map(InternalPlugin::getDescriptor)
                .filter(p -> p.getUpdater() != null)
                .forEach(p -> checkPluginUpdates(p.getId()));
    }

    private void checkEntry(
            String componentId,
            String name,
            String current,
            UpdateConfiguration spec,
            UpdaterSettings cfg
    ) {
        // Nothing configured?
        if (spec.getRelease() == null && spec.getDev() == null) return;

        // 1) LOCAL DEV exact build
        if ("dev".equalsIgnoreCase(current)) {
            if (cfg.isWarnAboutLocalBuilds()) {
                log.warn("[Updater] You are running a local dev build of {}.", name);

                eventPublisher.publishEvent(new UpdateLocalNotificationEvent(
                        componentId,
                        name,
                        current
                ));
            }
            return;
        }

        // 2) branch/CI build (anything not matching release-pattern)
        if (!isReleaseVersion(current)) {
            if (cfg.isWarnAboutDevBuilds() && spec.getDev() != null) {
                UpdateSource devSpec = spec.getDev();
                providerRegistry.by(devSpec).ifPresent(provider ->
                        warnAheadBehindBranches(componentId, current, name, provider, devSpec)
                );
            }
            return;
        }

        // 3) release build
        if (cfg.isWarnAboutUpdates() && spec.getRelease() != null) {
            UpdateSource relSpec = spec.getRelease();
            providerRegistry.by(relSpec).ifPresent(provider ->
                    checkReleaseUpdate(componentId, name, current, provider, relSpec)
            );
        }
    }

    private void checkReleaseUpdate(
            String componentId,
            String name,
            String current,
            UpdateProvider provider,
            UpdateSource source
    ) {
        try {
            Optional<String> latestOpt = provider.fetchLatest(source);

            if (latestOpt.isEmpty()) {
                log.debug("[Updater] No releases found for {}", name);
                return;
            }

            String latest = latestOpt.get();

            if (!bothPureSemver(current, latest)) {
                log.debug(
                        "[Updater] Skipping semver compare for {}: current=\"{}\", latest=\"{}\"",
                        name, current, latest
                );
                return;
            }

            if (compareSemver(latest, current) > 0) {
                log.warn("[Updater] The version of {} you are using is outdated. Current: {}, Latest: {}",
                        name, current, latest);

                eventPublisher.publishEvent(new UpdateAvailableEvent(
                        componentId,
                        name,
                        current,
                        latest,
                        false
                ));
                return;
            }

            log.info("[Updater] You are using the latest version of {}.", name);
        } catch (IOException ex) {
            log.debug("[Updater] Failed to check for new releases for {}. {}", name, ex.getMessage());
            eventPublisher.publishEvent(new UpdateCheckFailedEvent(componentId, ex));
        }
    }

    private void warnAheadBehindBranches(
            String componentId,
            String version,
            String name,
            UpdateProvider provider,
            UpdateSource source
    ) {
        String prefix = version.substring(version.lastIndexOf('-') + 1).toLowerCase();

        try {
            List<String> updates = provider.fetchRecentUpdates(source, BRANCH_UPDATE_LIMIT);
            int behind = 0;
            for (String id : updates) {
                if (id.toLowerCase().startsWith(prefix)) break;
                behind++;
            }

            if (behind == 0) {
                log.info("[Updater] You are using the latest dev build of {}.", name);
                return;
            }

	        log.warn("[Updater] You are {} commit{} behind the latest dev build of {}.",
                    behind, behind == 1 ? "" : "s", name);

            eventPublisher.publishEvent(new UpdateAvailableEvent(
                    componentId,
                    name,
                    version,
                    updates.getFirst(),
                    true,
                    behind
            ));
        } catch (IOException ex) {
            log.debug("[Updater] Failed to check for new dev builds for {}. {}", name, ex.getMessage());
            eventPublisher.publishEvent(new UpdateCheckFailedEvent(componentId, ex));
        }
    }

    /**
     * Matches "1.2.3" or "1.2.3-RC1"
     */
    static boolean isReleaseVersion(String v) {
        return v.matches("\\d+\\.\\d+\\.\\d+(?:-[0-9A-Za-z.-]+)?");
    }

    /**
     * true only if both sides are pure "x.y.z" (no suffix)
     */
    static boolean bothPureSemver(String a, String b) {
        return a.matches("\\d+\\.\\d+\\.\\d+") &&
                b.matches("\\d+\\.\\d+\\.\\d+");
    }

    /**
     * Compares "x.y.z" numerically; caller ensures both match the pattern
     */
    static int compareSemver(String a, String b) {
        String[] xa = a.split("\\."), xb = b.split("\\.");
        for (int i = 0; i < 3; i++) {
            int diff = Integer.parseInt(xa[i]) - Integer.parseInt(xb[i]);
            if (diff != 0) return diff;
        }
        return 0;
    }
}
