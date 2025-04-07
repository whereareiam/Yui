package me.whereareiam.yue.api.model.config.settings;

import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.List;

public class DiscordSettings {
	private String guildId;
	private String token;
	private List<GatewayIntent> intents;

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

	public List<GatewayIntent> getIntents() {
		return intents;
	}

	public void setIntents(List<GatewayIntent> intents) {
		this.intents = intents;
	}
}