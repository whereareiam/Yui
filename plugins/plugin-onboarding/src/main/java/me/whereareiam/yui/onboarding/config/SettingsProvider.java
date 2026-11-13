package me.whereareiam.yui.onboarding.config;

import me.whereareiam.yui.api.config.PluginConfigProvider;
import me.whereareiam.yui.onboarding.model.OnboardingSettings;
import org.springframework.stereotype.Component;

/**
 * Reads {@code settings.yml}.
 */
@Component
public class SettingsProvider extends PluginConfigProvider<OnboardingSettings> {
	@Override
	protected OnboardingSettings load() {
		return read("settings", new OnboardingSettings());
	}

	@Override
	public Class<OnboardingSettings> getObjectType() {
		return OnboardingSettings.class;
	}
}
