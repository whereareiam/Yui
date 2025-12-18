package me.whereareiam.yui.common.config.provider;

import me.whereareiam.configura.Config;
import me.whereareiam.yui.common.config.template.SettingsTemplate;
import me.whereareiam.yui.model.config.settings.Settings;
import org.springframework.stereotype.Component;

@Component
public class SettingsProvider extends DefaultConfigProvider<Settings> {
	@Override
	protected Settings load() {
		return Config.update(getBasePath().resolve("settings"), Settings.class);
	}

	@Override
	protected void registerTemplate() {
		Config.registerTemplate(SettingsTemplate.class);
	}

	@Override
	public Class<Settings> getObjectType() {
		return Settings.class;
	}
}
