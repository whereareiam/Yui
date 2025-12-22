package me.whereareiam.yui.common.localization.base;

import me.whereareiam.yui.localization.base.LocalizationLoaderBase;
import me.whereareiam.yui.localization.format.FileFormats;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LocalizationLoaderBaseTest {
    @Test
    void parseLocaleFromFilename_validLocale_returnsLocale(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("en-US.yml");
        Files.createFile(file);

        DiscordLocale locale = LocalizationLoaderBase.parseLocaleFromFilename(file);

        assertEquals(DiscordLocale.ENGLISH_US, locale);
    }

    @Test
    void parseLocaleFromFilename_invalidLocale_returnsNull(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("invalid-locale.yml");
        Files.createFile(file);

        DiscordLocale locale = LocalizationLoaderBase.parseLocaleFromFilename(file);

        assertNull(locale);
    }

    @Test
    void parseLocaleFromFilename_unknownLocale_returnsNull(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("UNKNOWN.yml");
        Files.createFile(file);

        DiscordLocale locale = LocalizationLoaderBase.parseLocaleFromFilename(file);

        assertNull(locale);
    }

    @Test
    void mergeMaps_simpleValues_overrides() {
        Map<String, Object> target = new HashMap<>();
        target.put("key1", "value1");
        target.put("key2", "value2");

        Map<String, Object> src = new HashMap<>();
        src.put("key2", "newValue2");
        src.put("key3", "value3");

        LocalizationLoaderBase.mergeMaps(target, src);

        assertEquals("value1", target.get("key1"));
        assertEquals("newValue2", target.get("key2"));
        assertEquals("value3", target.get("key3"));
    }

    @Test
    void mergeMaps_nestedMaps_mergesRecursively() {
        Map<String, Object> nested1 = new HashMap<>();
        nested1.put("a", "1");
        nested1.put("b", "2");

        Map<String, Object> target = new HashMap<>();
        target.put("nested", nested1);

        Map<String, Object> nested2 = new HashMap<>();
        nested2.put("b", "3");
        nested2.put("c", "4");

        Map<String, Object> src = new HashMap<>();
        src.put("nested", nested2);

        LocalizationLoaderBase.mergeMaps(target, src);

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) target.get("nested");
        assertEquals("1", result.get("a"));
        assertEquals("3", result.get("b"));
        assertEquals("4", result.get("c"));
    }

    @Test
    void detectFileFormat_localeFilename_returnsLOCALE(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("en-US.yml");
        Files.writeString(file, "key: value");

        var format = LocalizationLoaderBase.detectFileFormat(file);

        assertEquals(FileFormats.LOCALE.getName(), format.getName());
    }

    @Test
    void detectFileFormat_multiLocaleStructure_returnsMULTI_LOCALE(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("vocabulary.yml");
        String content = """
                cancel:
                  en-US: Cancel
                  de: Abbrechen
                """;
        Files.writeString(file, content);

        var format = LocalizationLoaderBase.detectFileFormat(file);

        assertEquals(FileFormats.MULTI_LOCALE.getName(), format.getName());
    }

    @Test
    void detectFileFormat_plainStructure_returnsTEMPLATE(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("templates.yml");
        String content = """
                greeting: Hello {name}
                farewell: Goodbye {name}
                """;
        Files.writeString(file, content);

        var format = LocalizationLoaderBase.detectFileFormat(file);

        assertEquals(FileFormats.TEMPLATE.getName(), format.getName());
    }
}
