package me.whereareiam.yue.api.model.profile;

import net.dv8tion.jda.api.interactions.DiscordLocale;

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

	public UserProfile(long id, DiscordLocale primaryLanguage, DiscordLocale[] additionalLanguages, long[] roles) {
		this.id = id;
		this.primaryLanguage = primaryLanguage;
		this.additionalLanguages = additionalLanguages;
	}

	public long getId() {
		return id;
	}

	public DiscordLocale getPrimaryLanguage() {
		return primaryLanguage;
	}

	public void setPrimaryLanguage(DiscordLocale primaryLanguage) {
		this.primaryLanguage = primaryLanguage;
	}

	public DiscordLocale[] getAdditionalLanguages() {
		return additionalLanguages;
	}

	public void setAdditionalLanguages(DiscordLocale[] additionalLanguages) {
		this.additionalLanguages = additionalLanguages;
	}

	public long[] getRoles() {
		return roles;
	}

	public void setRoles(long[] roles) {
		this.roles = roles;
	}
}
