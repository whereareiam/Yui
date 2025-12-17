package me.whereareiam.yui.common.config.provider;

import me.whereareiam.configura.Config;
import me.whereareiam.yui.common.config.template.SettingsTemplate;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.registry.Registry;
import me.whereareiam.yui.Reloadable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class SettingsProvider extends DefaultConfigProvider<Settings> {
	@Autowired
	public SettingsProvider(
			@Qualifier("dataPath") Path dataPath,
			Registry<Reloadable> registry
	) {
		super(dataPath, registry);
	}

	@Override
	protected Settings load() {
		return Config.update(getBasePath().resolve("settings"), Settings.class);
	}

	@Override
	protected void registerTemplate() {
		Config.registerTemplate(SettingsTemplate.class);
	}
}