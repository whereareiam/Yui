package me.whereareiam.yui.verification.listener;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.event.fluctlight.FluctlightClearedEvent;
import me.whereareiam.yui.verification.model.VerificationSettings;
import me.whereareiam.yui.verification.service.DefaultVerificationService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * A cleared profile ({@code /clear}) is no longer verified; it verifies again when {@code member-cleared} says so.
 */
@Component
@RequiredArgsConstructor
public class ProfileListener {
	private final DefaultVerificationService verification;
	private final ObjectProvider<VerificationSettings> settings;

	@TransactionalEventListener
	public void onCleared(FluctlightClearedEvent event) {
		verification.reset(event.getFluctlight().getId(), settings.getObject().getTriggers().isMemberCleared());
	}
}
