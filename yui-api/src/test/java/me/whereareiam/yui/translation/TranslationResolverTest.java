package me.whereareiam.yui.translation;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TranslationResolverTest {

    @Test
    void isTranslationKey_withValidPattern_returnsTrue() {
        assertTrue(TranslationResolver.isTranslationKey("translate(vocabulary.cancel)"));
        assertTrue(TranslationResolver.isTranslationKey("translate(commands.help.description)"));
    }

    @Test
    void isTranslationKey_withPlainText_returnsFalse() {
        assertFalse(TranslationResolver.isTranslationKey("Plain text"));
        assertFalse(TranslationResolver.isTranslationKey("Cancel"));
        assertFalse(TranslationResolver.isTranslationKey(""));
    }

    @Test
    void isTranslationKey_withInvalidPattern_returnsFalse() {
        assertFalse(TranslationResolver.isTranslationKey("translate()"));
        assertFalse(TranslationResolver.isTranslationKey("translate("));
        assertFalse(TranslationResolver.isTranslationKey("(vocabulary.cancel)"));
        assertFalse(TranslationResolver.isTranslationKey(null));
    }

    @Test
    void resolve_withNullText_returnsNull() {
        assertNull(TranslationResolver.resolve(null, DiscordLocale.ENGLISH_US));
        assertNull(TranslationResolver.resolve(null, 123L));
        assertNull(TranslationResolver.resolve(null));
    }

    @Test
    void resolve_withBlankText_returnsBlank() {
        assertEquals("", TranslationResolver.resolve("", DiscordLocale.ENGLISH_US));
        assertEquals("   ", TranslationResolver.resolve("   ", DiscordLocale.ENGLISH_US));
    }

    @Test
    void resolve_withPlainText_returnsAsIs() {
        String plainText = "Cancel Button";
        assertEquals(plainText, TranslationResolver.resolve(plainText, DiscordLocale.ENGLISH_US));
        assertEquals(plainText, TranslationResolver.resolve(plainText, 123L));
        assertEquals(plainText, TranslationResolver.resolve(plainText));
    }

    @Test
    void resolve_withTranslationKey_callsTranslatable() {
        String translationPattern = "translate(vocabulary.cancel)";
        String result = TranslationResolver.resolve(translationPattern, DiscordLocale.ENGLISH_US);
        assertNotEquals(translationPattern, result);
    }

    @Test
    void resolve_withTranslationKeyAndSpaces_trimsKey() {
        String patternWithSpaces = "translate( vocabulary.cancel )";
        String result = TranslationResolver.resolve(patternWithSpaces, DiscordLocale.ENGLISH_US);
        assertNotEquals(patternWithSpaces, result);
    }
}
