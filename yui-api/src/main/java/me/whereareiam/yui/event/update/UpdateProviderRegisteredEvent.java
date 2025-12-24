package me.whereareiam.yui.event.update;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Event published when an update provider is registered.
 */
@Getter
@RequiredArgsConstructor
public class UpdateProviderRegisteredEvent {
    private final String providerId;
}
