package me.whereareiam.yui.translation;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TranslationResolverTest {

    @Test
    void resolve_withNullText_returnsNull() {
        assertNull(TranslationResolver.text(null).resolve(DiscordLocale.ENGLISH_US));
        assertNull(TranslationResolver.text(null).resolve(123L));
        assertNull(TranslationResolver.text(null).resolveDefault());
    }

    @Test
    void resolve_withBlankText_returnsBlank() {
        assertEquals("", TranslationResolver.text("").resolve(DiscordLocale.ENGLISH_US));
        assertEquals("   ", TranslationResolver.text("   ").resolve(DiscordLocale.ENGLISH_US));
    }

    @Test
    void resolve_withPlainText_returnsAsIs() {
        String plainText = "Cancel Button";
        assertEquals(plainText, TranslationResolver.text(plainText).resolve(DiscordLocale.ENGLISH_US));
        assertEquals(plainText, TranslationResolver.text(plainText).resolve(123L));
        assertEquals(plainText, TranslationResolver.text(plainText).resolveDefault());
    }

    @Test
    void resolve_withTranslationKey_callsTranslatable() {
        String translationPattern = "translate(vocabulary.cancel)";
        String result = TranslationResolver.text(translationPattern).resolve(DiscordLocale.ENGLISH_US);
        assertNotEquals(translationPattern, result);
    }

    @Test
    void resolve_withTranslationKeyAndSpaces_trimsKey() {
        String patternWithSpaces = "translate( vocabulary.cancel )";
        String result = TranslationResolver.text(patternWithSpaces).resolve(DiscordLocale.ENGLISH_US);
        assertNotEquals(patternWithSpaces, result);
    }
}
