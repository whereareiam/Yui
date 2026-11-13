package me.whereareiam.yui.model.config.settings;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * The channels Yui posts to, until they move into {@code conversations.yml} and {@code audit.yml}.
 */
@Getter
@Setter
public class DiscordSettings {
	private Channels channels;

	@Getter
	@Setter
	public static class Channels {
		private List<String> tempChannelCategories;

		/**
		 * Map of audit type -> channel id(s).
		 * Supports either a single channel ID or multiple IDs in config.
		 */
		private Map<String, List<String>> audit;
	}
}
