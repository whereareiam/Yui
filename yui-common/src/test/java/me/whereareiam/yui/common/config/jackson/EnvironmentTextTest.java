package me.whereareiam.yui.common.config.jackson;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnvironmentTextTest {
	private static final Map<String, String> ENVIRONMENT = Map.of("DISCORD_TOKEN", "abc$1", "HOST", "db");

	@Test
	void resolve_ReplacesEachVariable() {
		assertEquals("abc$1", EnvironmentText.resolve("${DISCORD_TOKEN}", ENVIRONMENT::get));
		assertEquals("jdbc://db:5432", EnvironmentText.resolve("jdbc://${HOST}:5432", ENVIRONMENT::get));
	}

	@Test
	void resolve_LeavesTextWithoutVariables() {
		assertEquals("plain $ text", EnvironmentText.resolve("plain $ text", ENVIRONMENT::get));
	}

	@Test
	void resolve_AVariableThatIsNotSet_Fails() {
		assertThrows(IllegalStateException.class, () -> EnvironmentText.resolve("${MISSING}", ENVIRONMENT::get));
	}
}
