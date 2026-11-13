package me.whereareiam.yui.example.config;

import me.whereareiam.yui.api.config.PluginConfigProvider;
import me.whereareiam.yui.example.model.GreetingSettings;
import org.springframework.stereotype.Component;

/**
 * Reads {@code settings.yml}, again on {@code /reload}.
 */
@Component
public class SettingsProvider extends PluginConfigProvider<GreetingSettings> {
	@Override
	protected GreetingSettings load() {
		return read("settings", new GreetingSettings());
	}

	@Override
	public Class<GreetingSettings> getObjectType() {
		return GreetingSettings.class;
	}
}
