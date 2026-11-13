package me.whereareiam.yui.api.event.update;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Event published when an update check fails.
 */
@Getter
@RequiredArgsConstructor
public class UpdateCheckFailedEvent {
    private final String componentId;
    private final Throwable cause;
}
