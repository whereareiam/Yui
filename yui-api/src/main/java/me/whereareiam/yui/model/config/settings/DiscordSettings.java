package me.whereareiam.yui.model.config.settings;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * The categories temporary conversation channels are made in, until they move into {@code conversations.yml}.
 */
@Getter
@Setter
public class DiscordSettings {
	private Channels channels;

	@Getter
	@Setter
	public static class Channels {
		private List<String> tempChannelCategories;
	}
}
