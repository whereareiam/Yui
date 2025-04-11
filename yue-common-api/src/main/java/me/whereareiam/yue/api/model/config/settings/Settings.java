package me.whereareiam.yue.api.model.config.settings;

import me.whereareiam.yue.api.model.config.settings.database.DatabaseSettings;

import java.util.Locale;

public class Settings {
	private Locale locale;
	private DiscordSettings discord;
	private DatabaseSettings database;
	
	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
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
