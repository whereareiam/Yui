package me.whereareiam.yui.onboarding.listener;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.event.fluctlight.FluctlightClearedEvent;
import me.whereareiam.yui.onboarding.model.OnboardingSettings;
import me.whereareiam.yui.onboarding.service.DefaultOnboardingService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * A cleared profile ({@code /clear}) is no longer verified; it verifies again when {@code member-cleared} says so.
 */
@Component
@RequiredArgsConstructor
public class ProfileListener {
	private final DefaultOnboardingService onboarding;
	private final ObjectProvider<OnboardingSettings> settings;

	@TransactionalEventListener
	public void onCleared(FluctlightClearedEvent event) {
		onboarding.reset(event.getFluctlight().getId(), settings.getObject().getTriggers().isMemberCleared());
	}
}
