package me.whereareiam.yui.event.update;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Event published when a component is running a local dev build.
 */
@Getter
@RequiredArgsConstructor
public class UpdateLocalNotificationEvent {
    private final String componentId;
    private final String componentName;
    private final String currentVersion;
}
