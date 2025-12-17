package me.whereareiam.yui.common.config.template;

import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.yui.model.config.settings.DiscordSettings;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.config.settings.database.DatabaseSettings;
import me.whereareiam.yui.model.config.settings.database.HikariSettings;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SettingsTemplate implements TemplateProvider<Settings> {
	@Override
	public Settings supply(Settings settings) {
		// Default values
		settings.setLocale(DiscordLocale.ENGLISH_US);

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

		DiscordSettings.Channels channels = new DiscordSettings.Channels();
		channels.setTempChannelCategories(List.of("SET_YOUR_TEMP_CHANNEL_CATEGORY_ID"));
		discordSettings.setChannels(channels);

		settings.setDiscord(discordSettings);

		DatabaseSettings databaseSettings = getDatabaseSettings();
		settings.setDatabase(databaseSettings);

		return settings;
	}

	private DatabaseSettings getDatabaseSettings() {
		DatabaseSettings databaseSettings = new DatabaseSettings();
		databaseSettings.setHostname("127.0.0.1");
		databaseSettings.setPort(5432);
		databaseSettings.setUsername("postgres");
		databaseSettings.setPassword("postgres");
		databaseSettings.setDatabase("yui");

		HikariSettings hikariSettings = new HikariSettings();
		hikariSettings.setConnectionTimeout(30000);
		hikariSettings.setIdleTimeout(600000);
		hikariSettings.setMaxLifetime(1800000);
		hikariSettings.setMaximumPoolSize(10);
		hikariSettings.setMinimumIdle(2);

		databaseSettings.setHikari(hikariSettings);
		return databaseSettings;
	}
}
