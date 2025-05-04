package me.whereareiam.yui.api.input.translation;

import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.Map;

/**
 * Interface for loading translation resources from various sources.
 * Implementations can provide translations from files, databases, or other resources.
 * <p>
 * Each loader returns a hierarchical map structure:
 * <ul>
 *   <li>The outer map keys are namespace prefixes (empty string for core translations,
 *       "plugin.{name}." for plugin-specific translations)</li>
 *   <li>The middle map contains {@link DiscordLocale} objects as keys for each supported language</li>
 *   <li>The inner map contains the actual translation key-value pairs, where keys are
 *       dot-notation paths and values are the translated strings</li>
 * </ul>
 * <p>
 * The {@link TranslationService} will merge results from all loaders into a unified
 * translation map, preserving the proper key prefixing.
 */
public interface TranslationLoader {
	/**
	 * Loads all translations from the implementing source.
	 *
	 * @return A map structure where:
	 * - The key is the namespace prefix (e.g., "", "plugin.music.")
	 * - The value is a map of locales to their translation key-value pairs
	 */
	Map<String, Map<DiscordLocale, Map<String, String>>> loadAll();
}