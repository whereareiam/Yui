package me.whereareiam.yue.api.output.service;

import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.List;

public interface LanguageService {
	void addLanguage(DiscordLocale locale);

	void removeLanguage(DiscordLocale locale);

	boolean languageExists(DiscordLocale locale);

	List<DiscordLocale> getAvailableLanguages();
}
