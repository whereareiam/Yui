package me.whereareiam.yue.api.model.config.settings;

import me.whereareiam.yue.api.model.config.settings.database.DatabaseSettings;
import net.dv8tion.jda.api.interactions.DiscordLocale;

public class Settings {
	private DiscordLocale locale;
	private DiscordSettings discord;
	private DatabaseSettings database;

	public DiscordLocale getLocale() {
		return locale;
	}

	public void setLocale(DiscordLocale locale) {
		this.locale = locale;
	}

	public DiscordSettings getDiscord() {
		return discord;
	}

	public void setDiscord(DiscordSettings discord) {
		this.discord = discord;
	}

	public DatabaseSettings getDatabase() {
		return database;
	}

	public void setDatabase(DatabaseSettings database) {
		this.database = database;
	}
}
