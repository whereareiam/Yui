package me.whereareiam.yue.adapter.config.template;

import me.whereareiam.yue.api.model.config.Roles;
import me.whereareiam.yue.api.output.config.DefaultConfig;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RolesTemplate implements DefaultConfig<Roles> {
	@Override
	public Roles getDefault() {
		Roles roles = new Roles();

		// Default values
		roles.setLanguageRoles(Map.of(
				DiscordLocale.ENGLISH_US.getLocale(), 1206929604971859978L,
				DiscordLocale.GERMAN.getLocale(), 1206929557987401740L,
				DiscordLocale.RUSSIAN.getLocale(), 1206929308355137597L
		));

		roles.setAllowedRoles(Map.of(
				"us", 1206929604971859978L,
				"de", 1206929557987401740L,
				"ru", 1206929308355137597L,
				"verified", 856952987896774696L
		));

		return roles;
	}
}
