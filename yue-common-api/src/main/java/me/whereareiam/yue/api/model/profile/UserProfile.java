package me.whereareiam.yue.api.model.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.interactions.DiscordLocale;

@Getter
@Setter
@AllArgsConstructor
public class UserProfile {
	private final long id;
	private DiscordLocale primaryLanguage;
	private DiscordLocale[] additionalLanguages;

	private long[] roles;

	public UserProfile(long id) {
		this.id = id;
	}

	public UserProfile(long id, DiscordLocale primaryLanguage) {
		this.id = id;
		this.primaryLanguage = primaryLanguage;
	}
}
