package me.whereareiam.yui.common.translation;

import me.whereareiam.semantica.translation.base.TranslationLocale;
import me.whereareiam.semantica.locale.LocaleParser;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiscordLocaleConverterTest {

    private LocaleParser<DiscordLocale> parser;

    @BeforeEach
    void setUp() {
        parser = DiscordLocaleAdapter::wrap;
    }

    @Test
    void testParser_parsesDiscordLocaleToTranslationLocale() {
        TranslationLocale result = parser.parse(DiscordLocale.ENGLISH_US);

        assertNotNull(result);
        assertEquals("en-US", result.getLanguage());
    }

    @Test
    void testParser_parsesGermanLocale() {
        TranslationLocale result = parser.parse(DiscordLocale.GERMAN);

        assertNotNull(result);
        assertEquals("de", result.getLanguage());
    }

    @Test
    void testParser_equalityForSameLocale() {
        TranslationLocale first = parser.parse(DiscordLocale.ENGLISH_US);
        TranslationLocale second = parser.parse(DiscordLocale.ENGLISH_US);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void testParser_inequalityForDifferentLocales() {
        TranslationLocale english = parser.parse(DiscordLocale.ENGLISH_US);
        TranslationLocale german = parser.parse(DiscordLocale.GERMAN);

        assertNotEquals(english, german);
    }

    @Test
    void testParser_toStringReturnsLocaleString() {
        TranslationLocale result = parser.parse(DiscordLocale.FRENCH);

        assertEquals("fr", result.toString());
    }
}
