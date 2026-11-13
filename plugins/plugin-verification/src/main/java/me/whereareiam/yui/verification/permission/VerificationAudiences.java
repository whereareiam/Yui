package me.whereareiam.yui.verification.permission;

import lombok.RequiredArgsConstructor;
import me.whereareiam.conditia.model.Requirements;
import me.whereareiam.yui.api.config.ConfigReader;
import me.whereareiam.yui.api.model.config.audience.Audiences;
import me.whereareiam.yui.api.permission.AudienceDefaults;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Verification's audience {@code verification:verified}: members who finished a verification.
 */
@Component
@RequiredArgsConstructor
public class VerificationAudiences implements AudienceDefaults {
	private final ConfigReader reader;

	@Override
	public @NotNull Audiences defaults() {
		return new Audiences(Map.of("verified", reader.parse("- verified: true", Requirements.class)));
	}
}
