package me.whereareiam.yui.model.config.settings;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for the update checking system.
 */
@Getter
@Setter
public class UpdaterSettings {
    /**
     * Whether to check for updates.
     */
    private boolean checkForUpdates = true;

    /**
     * Whether to warn about available updates.
     */
    private boolean warnAboutUpdates = true;

    /**
     * Whether to warn about local development builds.
     */
    private boolean warnAboutLocalBuilds = true;

    /**
     * Whether to warn about development/snapshot builds.
     */
    private boolean warnAboutDevBuilds = true;

    /**
     * Whether to check plugin updates in addition to core updates.
     */
    private boolean checkPluginUpdates = true;

    /**
     * Update check interval in hours.
     */
    private int interval = 24;
}
