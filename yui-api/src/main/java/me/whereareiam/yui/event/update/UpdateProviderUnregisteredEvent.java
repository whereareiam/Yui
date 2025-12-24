package me.whereareiam.yui.event.update;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Event published when an update provider is unregistered.
 */
@Getter
@RequiredArgsConstructor
public class UpdateProviderUnregisteredEvent {
    private final String providerId;
}
