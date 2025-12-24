package me.whereareiam.yui.common.audit;

import me.whereareiam.configura.type.MultiValue;
import me.whereareiam.yui.model.config.settings.DiscordSettings;
import me.whereareiam.yui.model.config.settings.Settings;
import net.dv8tion.jda.api.JDA;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for wildcard matching in DefaultAuditService.
 */
class AuditServiceTest {
    private DefaultAuditService auditService;
	private DiscordSettings.Channels mockChannels;

	@BeforeEach
    void setUp() {
	    ObjectProvider<Settings> mockSettingsProvider = mock(ObjectProvider.class);
	    Settings mockSettings = mock(Settings.class);
	    DiscordSettings mockDiscordSettings = mock(DiscordSettings.class);
        mockChannels = mock(DiscordSettings.Channels.class);
		JDA mockJDA = mock(JDA.class);

        when(mockSettingsProvider.getObject()).thenReturn(mockSettings);
        when(mockSettings.getDiscord()).thenReturn(mockDiscordSettings);
        when(mockDiscordSettings.getChannels()).thenReturn(mockChannels);

        auditService = new DefaultAuditService(mockSettingsProvider, mockJDA);
    }

    @Test
    void testExactMatch_takesPriorityOverWildcard() {
        Map<String, MultiValue<String>> auditConfig = new HashMap<>();
        auditConfig.put("update_plugin_*_available", MultiValue.of("wildcard-channel"));
        auditConfig.put("update_plugin_specific_available", MultiValue.of("exact-channel"));

        when(mockChannels.getAudit()).thenReturn(auditConfig);

        // Exact match should be used
        Optional<List<String>> result = auditService.getChannelIds("update_plugin_specific_available", null);
        assertTrue(result.isPresent());
        assertEquals("exact-channel", result.get().getFirst());
    }

    @Test
    void testWildcard_singleStar() {
        Map<String, MultiValue<String>> auditConfig = new HashMap<>();
        auditConfig.put("update_plugin_*_available", MultiValue.of("channel-1"));

        when(mockChannels.getAudit()).thenReturn(auditConfig);

        // Should match plugin with any name
        Optional<List<String>> result1 = auditService.getChannelIds("update_plugin_yuiverification_available", null);
        assertTrue(result1.isPresent());
        assertEquals("channel-1", result1.get().getFirst());

        Optional<List<String>> result2 = auditService.getChannelIds("update_plugin_another_available", null);
        assertTrue(result2.isPresent());
        assertEquals("channel-1", result2.get().getFirst());
    }

    @Test
    void testWildcard_multipleParts() {
        Map<String, MultiValue<String>> auditConfig = new HashMap<>();
        auditConfig.put("update_*_*_available", MultiValue.of("channel-1"));

        when(mockChannels.getAudit()).thenReturn(auditConfig);

        // Should match both plugin and core updates
        Optional<List<String>> result1 = auditService.getChannelIds("update_plugin_test_available", null);
        assertTrue(result1.isPresent());

        Optional<List<String>> result2 = auditService.getChannelIds("update_core_yui_available", null);
        assertTrue(result2.isPresent());
    }

    @Test
    void testWildcard_atStart() {
        Map<String, MultiValue<String>> auditConfig = new HashMap<>();
        auditConfig.put("*_available", MultiValue.of("channel-1"));

        when(mockChannels.getAudit()).thenReturn(auditConfig);

        Optional<List<String>> result1 = auditService.getChannelIds("update_plugin_available", null);
        assertTrue(result1.isPresent());

        Optional<List<String>> result2 = auditService.getChannelIds("anything_available", null);
        assertTrue(result2.isPresent());
    }

    @Test
    void testWildcard_atEnd() {
        Map<String, MultiValue<String>> auditConfig = new HashMap<>();
        auditConfig.put("update_*", MultiValue.of("channel-1"));

        when(mockChannels.getAudit()).thenReturn(auditConfig);

        Optional<List<String>> result1 = auditService.getChannelIds("update_available", null);
        assertTrue(result1.isPresent());

        Optional<List<String>> result2 = auditService.getChannelIds("update_anything", null);
        assertTrue(result2.isPresent());
    }

    @Test
    void testWildcard_noMatch() {
        Map<String, MultiValue<String>> auditConfig = new HashMap<>();
        auditConfig.put("update_plugin_*_available", MultiValue.of("channel-1"));

        when(mockChannels.getAudit()).thenReturn(auditConfig);

        // Should not match - different structure
        Optional<List<String>> result = auditService.getChannelIds("update_core_available", null);
        assertFalse(result.isPresent());
    }

    @Test
    void testWildcard_specialCharactersEscaped() {
        Map<String, MultiValue<String>> auditConfig = new HashMap<>();
        auditConfig.put("update.plugin.*", MultiValue.of("channel-1"));

        when(mockChannels.getAudit()).thenReturn(auditConfig);

        // Dot should be treated literally, not as regex wildcard
        Optional<List<String>> result1 = auditService.getChannelIds("update.plugin.test", null);
        assertTrue(result1.isPresent());

        // Should not match if dot is different character
        Optional<List<String>> result2 = auditService.getChannelIds("updateXplugin.test", null);
        assertFalse(result2.isPresent());
    }

    @Test
    void testIsConfigured_exactMatch() {
        Map<String, MultiValue<String>> auditConfig = new HashMap<>();
        auditConfig.put("user_join", MultiValue.of("channel-1"));

        when(mockChannels.getAudit()).thenReturn(auditConfig);

        assertTrue(auditService.isConfigured("user_join"));
        assertFalse(auditService.isConfigured("user_leave"));
    }

    @Test
    void testIsConfigured_wildcardMatch() {
        Map<String, MultiValue<String>> auditConfig = new HashMap<>();
        auditConfig.put("update_*_available", MultiValue.of("channel-1"));

        when(mockChannels.getAudit()).thenReturn(auditConfig);

        assertTrue(auditService.isConfigured("update_plugin_available"));
        assertTrue(auditService.isConfigured("update_core_available"));
        assertFalse(auditService.isConfigured("user_join"));
    }

    @Test
    void testMultipleWildcards_firstMatchWins() {
        Map<String, MultiValue<String>> auditConfig = new LinkedHashMap<>();
        auditConfig.put("update_*_available", MultiValue.of("channel-1"));
        auditConfig.put("update_plugin_*", MultiValue.of("channel-2"));

        when(mockChannels.getAudit()).thenReturn(auditConfig);

        // The first matching pattern should be used
        Optional<List<String>> result = auditService.getChannelIds("update_plugin_available", null);
        assertTrue(result.isPresent());
        assertEquals("channel-1", result.get().getFirst());
    }
}
