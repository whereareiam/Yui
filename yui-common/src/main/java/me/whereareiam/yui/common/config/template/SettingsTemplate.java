package me.whereareiam.yui.common.config.template;

import me.whereareiam.configura.merge.defaults.DefaultsProvider;
import me.whereareiam.yui.model.config.settings.DiscordSettings;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.config.settings.UpdaterSettings;
import me.whereareiam.yui.model.type.Duration;
import me.whereareiam.yui.type.update.UpdateCheck;
import me.whereareiam.yui.type.update.UpdateWarning;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

/**
 * The values a {@code settings.yml} setting takes when the file does not name it.
 */
@Component
public class SettingsTemplate implements DefaultsProvider<Settings> {
	@Override
	public Settings supply(Settings settings) {
		settings.setToken("SET_YOUR_TOKEN");
		settings.setGuild("SET_YOUR_GUILD_ID");
		settings.setIntents(List.of(
				GatewayIntent.GUILD_MESSAGES,
				GatewayIntent.MESSAGE_CONTENT,
				GatewayIntent.GUILD_MEMBERS,
				GatewayIntent.GUILD_PRESENCES,
				GatewayIntent.GUILD_VOICE_STATES,
				GatewayIntent.GUILD_EXPRESSIONS,
				GatewayIntent.SCHEDULED_EVENTS
		));
		settings.setLocale(DiscordLocale.ENGLISH_US);
		settings.setUpdater(updater());
		settings.setDiscord(discord());

		return settings;
	}

	private static UpdaterSettings updater() {
		UpdaterSettings updater = new UpdaterSettings();
		updater.setInterval(Duration.parse("1d"));
		updater.setCheck(EnumSet.allOf(UpdateCheck.class));
		updater.setWarn(EnumSet.allOf(UpdateWarning.class));

		return updater;
	}

	private static DiscordSettings discord() {
		DiscordSettings.Channels channels = new DiscordSettings.Channels();
		channels.setTempChannelCategories(List.of("SET_YOUR_TEMP_CHANNEL_CATEGORY_ID"));

		DiscordSettings discord = new DiscordSettings();
		discord.setChannels(channels);

		return discord;
	}
}
