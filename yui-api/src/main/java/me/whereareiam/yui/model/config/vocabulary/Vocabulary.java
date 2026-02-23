package me.whereareiam.yui.model.config.vocabulary;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.HashMap;

/**
 * Model for vocabulary.yml file structure.
 * Contains common UI words with translations for multiple locales.
 */
@Getter
@Setter
public class Vocabulary {
    private LocaleMap cancel;
    private LocaleMap proceed;
    private LocaleMap confirm;
    private LocaleMap edit;
    private LocaleMap back;
    private LocaleMap next;
    private LocaleMap yes;
    private LocaleMap no;
    private Category category;

    @Getter
    @Setter
    public static class Category {
        private LocaleMap utility;
        private LocaleMap fun;
        private LocaleMap moderation;
        private LocaleMap administration;
        private LocaleMap none;
    }

    /**
     * Helper class to represent a map of locale codes to translations.
     * Extends HashMap to allow fluent API for easy initialization.
     */
    public static class LocaleMap extends HashMap<DiscordLocale, String> {
        public LocaleMap with(DiscordLocale locale, String translation) {
            super.put(locale, translation);
            return this;
        }
    }
}
