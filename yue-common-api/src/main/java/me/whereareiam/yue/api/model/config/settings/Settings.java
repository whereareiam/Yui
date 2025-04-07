package me.whereareiam.yue.api.model.config.settings;

import me.whereareiam.yue.api.model.config.settings.database.DatabaseSettings;

public class Settings {
	private DiscordSettings discord;
	private DatabaseSettings database;

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
