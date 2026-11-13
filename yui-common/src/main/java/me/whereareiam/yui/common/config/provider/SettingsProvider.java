package me.whereareiam.yui.common.config.provider;

import me.whereareiam.yui.common.config.template.SettingsTemplate;
import me.whereareiam.yui.model.config.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SettingsProvider extends DefaultConfigProvider<Settings> {
	@Autowired
	private SettingsTemplate template;

	@Override
	protected Settings load() {
		return read("settings", template.supply(new Settings()));
	}

	@Override
	public Class<Settings> getObjectType() {
		return Settings.class;
	}
}
