package me.whereareiam.yui.model.config.settings;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.model.type.Duration;

/**
 * Configuration for the update checking system.
 */
@Getter
@Setter
public class UpdaterSettings {
    /**
     * Whether to check for updates.
     */
    private boolean checkForUpdates;

    /**
     * Whether to warn about available updates.
     */
    private boolean warnAboutUpdates;

    /**
     * Whether to warn about local development builds.
     */
    private boolean warnAboutLocalBuilds;

    /**
     * Whether to warn about development/snapshot builds.
     */
    private boolean warnAboutDevBuilds;

    /**
     * Whether to check plugin updates in addition to core updates.
     */
    private boolean checkPluginUpdates;

    /**
     * Update check interval (e.g., "24h", "1d").
     */
    private Duration interval;
}
