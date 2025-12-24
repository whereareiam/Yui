package me.whereareiam.yui.event.update;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Event published when an update is available for a component.
 */
@Getter
@RequiredArgsConstructor
public class UpdateAvailableEvent {
    private final String componentId;
    private final String componentName;
    private final String currentVersion;
    private final String latestVersion;
    private final boolean devBuild;
    private final Integer commitsBehind;

    public UpdateAvailableEvent(
            String componentId,
            String componentName,
            String currentVersion,
            String latestVersion,
            boolean devBuild
    ) {
        this(componentId, componentName, currentVersion, latestVersion, devBuild, null);
    }
}
