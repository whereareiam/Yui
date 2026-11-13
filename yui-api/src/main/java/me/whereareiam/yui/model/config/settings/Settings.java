package me.whereareiam.yui.model.config.settings;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.List;

/**
 * {@code settings.yml}: how Yui connects to Discord, the guild it serves, its default locale and its updater.
 *
 * <p>Values may name environment variables ({@code token: "${DISCORD_TOKEN}"}), so secrets stay out of the file.</p>
 */
@Getter
@Setter
public class Settings {
	private String token;
	private String guild;
	private List<GatewayIntent> intents;
	private DiscordLocale locale;
	private UpdaterSettings updater;

	/**
	 * The channels of conversations, until they move into {@code conversations.yml}.
	 */
	private DiscordSettings discord;
}
