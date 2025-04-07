package me.whereareiam.yue.api.model.config.settings;

public class Settings {
	private Integer debugLevel; // 0-3

	private DiscordSettings discord;

	public Integer getDebugLevel() {
		return debugLevel;
	}

	public void setDebugLevel(Integer debugLevel) {
		this.debugLevel = debugLevel;
	}

	public DiscordSettings getDiscord() {
		return discord;
	}

	public void setDiscord(DiscordSettings discord) {
		this.discord = discord;
	}
}
