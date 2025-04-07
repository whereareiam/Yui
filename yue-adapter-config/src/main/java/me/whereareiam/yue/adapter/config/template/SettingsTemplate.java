package me.whereareiam.yue.adapter.config.template;

import me.whereareiam.yue.api.model.config.settings.DiscordSettings;
import me.whereareiam.yue.api.model.config.settings.Settings;
import me.whereareiam.yue.api.output.config.DefaultConfig;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SettingsTemplate implements DefaultConfig<Settings> {
	@Override
	public Settings getDefault() {
		Settings settings = new Settings();

		// Default values
		DiscordSettings discordSettings = new DiscordSettings();
		discordSettings.setGuildId("SET_YOUR_GUILD_ID");
		discordSettings.setToken("SET_YOUR_TOKEN");
		discordSettings.setIntents(List.of(
				GatewayIntent.GUILD_MESSAGES,
				GatewayIntent.MESSAGE_CONTENT,
				GatewayIntent.GUILD_MEMBERS,
				GatewayIntent.GUILD_PRESENCES,
				GatewayIntent.GUILD_VOICE_STATES,
				GatewayIntent.GUILD_EXPRESSIONS,
				GatewayIntent.SCHEDULED_EVENTS
		));

		settings.setDiscord(discordSettings);

		return settings;
	}
}
