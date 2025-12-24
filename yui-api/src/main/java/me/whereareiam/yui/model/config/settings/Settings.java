package me.whereareiam.yui.model.config.settings;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.model.config.settings.database.DatabaseSettings;
import net.dv8tion.jda.api.interactions.DiscordLocale;

@Getter
@Setter
public class Settings {
	private DiscordLocale locale;
	private DiscordSettings discord;
	private DatabaseSettings database;
	private UpdaterSettings updater;
}
