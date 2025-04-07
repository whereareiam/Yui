package me.whereareiam.yue.adapter.config.template;

import me.whereareiam.yue.api.model.config.settings.DiscordSettings;
import me.whereareiam.yue.api.model.config.settings.Settings;
import me.whereareiam.yue.api.output.config.DefaultConfig;
import org.springframework.stereotype.Component;

@Component
public class SettingsTemplate implements DefaultConfig<Settings> {
	@Override
	public Settings getDefault() {
		Settings settings = new Settings();

		// Default values
		settings.setDebugLevel(2);

		DiscordSettings discordSettings = new DiscordSettings();
		discordSettings.setGuildId("SET_YOUR_GUILD_ID");
		discordSettings.setToken("SET_YOUR_TOKEN");

		settings.setDiscord(discordSettings);

		return settings;
	}
}
