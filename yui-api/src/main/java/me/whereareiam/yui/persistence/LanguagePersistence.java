package me.whereareiam.yui.persistence;

import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.List;

public interface LanguagePersistence {
	void addLanguage(DiscordLocale locale);

	void removeLanguage(DiscordLocale locale);

	boolean languageExists(DiscordLocale locale);

	List<DiscordLocale> getAvailableLanguages();
}
