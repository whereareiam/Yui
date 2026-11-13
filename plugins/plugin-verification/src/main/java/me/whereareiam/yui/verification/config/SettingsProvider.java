package me.whereareiam.yui.verification.config;

import me.whereareiam.yui.api.config.PluginConfigProvider;
import me.whereareiam.yui.verification.model.VerificationSettings;
import org.springframework.stereotype.Component;

/**
 * Reads {@code settings.yml}.
 */
@Component
public class SettingsProvider extends PluginConfigProvider<VerificationSettings> {
	@Override
	protected VerificationSettings load() {
		return read("settings", new VerificationSettings());
	}

	@Override
	public Class<VerificationSettings> getObjectType() {
		return VerificationSettings.class;
	}
}
