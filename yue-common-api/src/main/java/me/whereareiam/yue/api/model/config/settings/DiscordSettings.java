package me.whereareiam.yue.api.model.config.settings;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.List;

@Getter
@Setter
public class DiscordSettings {
	private String guildId;
	private String token;
	private List<GatewayIntent> intents;
}