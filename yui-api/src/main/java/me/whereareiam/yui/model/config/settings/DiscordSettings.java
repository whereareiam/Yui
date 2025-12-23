package me.whereareiam.yui.model.config.settings;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import me.whereareiam.configura.type.MultiValue;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class DiscordSettings {
	private String guildId;
	private String token;
	private List<GatewayIntent> intents;
	private Channels channels;

	@Getter
	@Setter
	public static class Channels {
		private List<String> tempChannelCategories;
		/**
		 * Map of audit type -> channel id(s).
		 * Supports either a single channel ID or multiple IDs in config.
		 */
		private Map<String, MultiValue<String>> audit;
	}
}