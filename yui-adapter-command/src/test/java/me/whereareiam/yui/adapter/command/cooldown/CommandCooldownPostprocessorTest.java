package me.whereareiam.yui.adapter.command.cooldown;

import me.whereareiam.yui.adapter.command.definition.CommandDefinitionRegistry;
import me.whereareiam.yui.api.exception.command.CooldownActiveException;
import me.whereareiam.yui.api.model.command.CommandCooldown;
import me.whereareiam.yui.api.model.type.Duration;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandCooldownPostprocessorTest {
	private final MutableClock clock = new MutableClock();
	private final CommandCooldownPostprocessor cooldowns = new CommandCooldownPostprocessor(new CommandDefinitionRegistry(), clock);
	private final CommandCooldown tenSeconds = new CommandCooldown(true, Duration.ofSeconds(10), null);

	@Test
	void aSecondUse_WithinTheCooldown_IsHeldBack() {
		cooldowns.start(1L, "clear", tenSeconds);
		clock.now = clock.now.plusSeconds(3);

		CooldownActiveException held = assertThrows(CooldownActiveException.class, () -> cooldowns.start(1L, "clear", tenSeconds));
		assertEquals(8, held.getSecondsLeft());
	}

	@Test
	void aUse_AfterTheCooldown_Runs() {
		cooldowns.start(1L, "clear", tenSeconds);
		clock.now = clock.now.plusSeconds(11);

		assertDoesNotThrow(() -> cooldowns.start(1L, "clear", tenSeconds));
	}

	@Test
	void commandsOfOneGroup_ShareTheirWait() {
		CommandCooldown grouped = new CommandCooldown(true, Duration.ofSeconds(10), "moderation");
		cooldowns.start(1L, "clear", grouped);

		assertThrows(CooldownActiveException.class, () -> cooldowns.start(1L, "ban", grouped));
		assertDoesNotThrow(() -> cooldowns.start(2L, "ban", grouped));
	}

	private static final class MutableClock extends Clock {
		private Instant now = Instant.parse("2026-11-13T12:00:00Z");

		@Override
		public ZoneOffset getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return now;
		}
	}
}
