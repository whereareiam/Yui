package me.whereareiam.yue.api.model.config.settings;

public class DiscordSettings {
	private String guildId;
	private String token;

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}