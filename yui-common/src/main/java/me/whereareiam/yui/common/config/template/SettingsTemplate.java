package me.whereareiam.yui.common.config.template;

import me.whereareiam.configura.merge.defaults.DefaultsProvider;
import me.whereareiam.yui.model.config.settings.DiscordSettings;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.config.settings.UpdaterSettings;
import me.whereareiam.yui.model.config.settings.database.DatabaseSettings;
import me.whereareiam.yui.model.config.settings.database.HikariSettings;
import me.whereareiam.yui.model.type.Duration;
import me.whereareiam.yui.type.DatabaseType;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SettingsTemplate implements DefaultsProvider<Settings> {
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
		channels.setAudit(Map.of(
				"user_join", List.of("SET_YOUR_AUDIT_CHANNEL_ID"),
				"user_leave", List.of("SET_YOUR_AUDIT_CHANNEL_ID"),
				"user_kick", List.of("SET_YOUR_AUDIT_CHANNEL_ID"),
				"update_*_available", List.of("SET_YOUR_AUDIT_CHANNEL_ID")
		));
		discordSettings.setChannels(channels);

		settings.setDiscord(discordSettings);

		DatabaseSettings databaseSettings = getDatabaseSettings();
		settings.setDatabase(databaseSettings);

		UpdaterSettings updaterSettings = getUpdaterSettings();
		settings.setUpdater(updaterSettings);

		return settings;
	}

	private DatabaseSettings getDatabaseSettings() {
		DatabaseSettings databaseSettings = new DatabaseSettings();
		databaseSettings.setType(DatabaseType.H2);
		databaseSettings.setFile("database/yui");
		databaseSettings.setHostname("127.0.0.1");
		databaseSettings.setPort(5432);
		databaseSettings.setDatabase("yui");
		databaseSettings.setUsername("yui");
		databaseSettings.setPassword("");

		HikariSettings hikariSettings = new HikariSettings();
		hikariSettings.setConnectionTimeout(30000);
		hikariSettings.setIdleTimeout(600000);
		hikariSettings.setMaxLifetime(1800000);
		hikariSettings.setMaximumPoolSize(10);
		hikariSettings.setMinimumIdle(2);

		databaseSettings.setHikari(hikariSettings);

		return databaseSettings;
	}

	private UpdaterSettings getUpdaterSettings() {
		UpdaterSettings updaterSettings = new UpdaterSettings();
		updaterSettings.setCheckForUpdates(true);
		updaterSettings.setWarnAboutUpdates(true);
		updaterSettings.setWarnAboutLocalBuilds(true);
		updaterSettings.setWarnAboutDevBuilds(true);
		updaterSettings.setCheckPluginUpdates(true);
		updaterSettings.setInterval(Duration.ofDays(1));

		return updaterSettings;
	}
}
