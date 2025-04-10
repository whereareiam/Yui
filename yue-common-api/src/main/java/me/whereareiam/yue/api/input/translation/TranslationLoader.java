package me.whereareiam.yue.api.input.translation;

import java.util.Locale;
import java.util.Map;

public interface TranslationLoader {
	/**
	 * Loads all translations for its domain(s).
	 * <p>
	 * For core:   returns a single entry "core" -> ...
	 * For modules: returns multiple entries "moduleName" -> ...
	 *
	 * @return domain -> (locale -> (key -> text))
	 */
	Map<String, Map<Locale, Map<String, String>>> loadAll();
}