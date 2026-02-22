package me.whereareiam.yui.common.journey.registry;

import me.whereareiam.yui.common.journey.JourneyDefinitionRegistry;
import me.whereareiam.yui.model.journey.JourneyInstruction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JourneyDefinitionRegistryTest {
	@Mock
	private ApplicationContext context;

	@Test
	void duplicateStepRegistrationThrows() {
		JourneyDefinitionRegistry registry = new JourneyDefinitionRegistry();

		registry.registerStep(context, "verification", "welcome", 10, "", _ -> JourneyInstruction.waitForSignal());

		assertThrows(IllegalStateException.class, () ->
				registry.registerStep(context, "verification", "welcome", 10, "", _ -> JourneyInstruction.waitForSignal()));
	}

	@Test
	void unknownGroupReferenceThrowsWhenLoadingDefinition() {
		JourneyDefinitionRegistry registry = new JourneyDefinitionRegistry();

		registry.registerStep(context, "verification", "additional", 20, "languages", _ -> JourneyInstruction.waitForSignal());

		assertThrows(IllegalStateException.class, () -> registry.get("verification"));
	}
}
