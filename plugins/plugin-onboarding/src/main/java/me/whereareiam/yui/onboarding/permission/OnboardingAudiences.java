package me.whereareiam.yui.onboarding.permission;

import lombok.RequiredArgsConstructor;
import me.whereareiam.conditia.model.Requirements;
import me.whereareiam.yui.api.config.ConfigReader;
import me.whereareiam.yui.api.model.config.audience.Audiences;
import me.whereareiam.yui.api.permission.AudienceDefaults;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Onboarding's audience {@code onboarding:verified}: members who finished onboarding.
 */
@Component
@RequiredArgsConstructor
public class OnboardingAudiences implements AudienceDefaults {
	private final ConfigReader reader;

	@Override
	public @NotNull Audiences defaults() {
		return new Audiences(Map.of("verified", reader.parse("- verified: true", Requirements.class)));
	}
}
