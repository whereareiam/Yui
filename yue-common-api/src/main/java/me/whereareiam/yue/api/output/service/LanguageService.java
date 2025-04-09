package me.whereareiam.yue.api.output.service;

import java.util.List;
import java.util.Locale;

public interface LanguageService {
	void addLanguage(Locale locale);

	void removeLanguage(Locale locale);

	boolean languageExists(Locale locale);

	List<Locale> getAvailableLanguages();
}
