package me.whereareiam.yui.verification.model;

import me.whereareiam.yui.api.model.type.Duration;
import me.whereareiam.yui.api.type.ConfigurationType;
import me.whereareiam.yui.common.config.ConfiguraConfiguration;
import me.whereareiam.yui.common.config.ConfiguraReader;
import me.whereareiam.yui.verification.type.DeadlineAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reads {@code settings.yml} the way Yui reads it for the plugin.
 */
class VerificationSettingsTest {
	@TempDir
	Path folder;

	private final ConfiguraReader reader = new ConfiguraReader(ConfiguraConfiguration.build(ConfigurationType.YAML));

	@Test
	void theAgreedShape_IsRead() throws IOException {
		Files.writeString(folder.resolve("settings.yml"), """
				triggers:
				  member-join: false
				  startup: true
				  member-cleared: false
				deadline:
				  enabled: true
				  after: 2d
				  action: nothing
				verified-role: "123456789012345678"
				""");

		VerificationSettings settings = reader.read(folder.resolve("settings"), new VerificationSettings());

		assertFalse(settings.getTriggers().isMemberJoin());
		assertFalse(settings.getTriggers().isMemberCleared());
		assertEquals(Duration.ofDays(2), settings.getDeadline().getAfter());
		assertEquals(DeadlineAction.NOTHING, settings.getDeadline().getAction());
		assertEquals("123456789012345678", settings.getVerifiedRole());
	}

	@Test
	void withoutAFile_EveryTriggerIsOnAndTheDeadlineKicksAfterADay() {
		VerificationSettings settings = reader.read(folder.resolve("settings"), new VerificationSettings());

		assertTrue(settings.getTriggers().isMemberJoin());
		assertEquals(Duration.ofDays(1), settings.getDeadline().getAfter());
		assertEquals(DeadlineAction.KICK, settings.getDeadline().getAction());
	}
}
