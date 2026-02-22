package me.whereareiam.yui.common.service;

import me.whereareiam.yui.model.Key;
import me.whereareiam.yui.model.component.ComponentAttributes;
import me.whereareiam.yui.plugin.PluginManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultInteractionServiceAttributesTest {
	private static final Key<String> TEST_KEY = Key.of("test.attribute", String.class);

	@Mock
	private JDA jda;
	@Mock
	private PluginManager pluginManager;

	private DefaultInteractionService interactions;

	@BeforeEach
	void setUp() {
		interactions = new DefaultInteractionService(jda, pluginManager);
	}

	@Test
	void storesAttributesForButtons() {
		ComponentAttributes attributes = ComponentAttributes.builder()
				.put(TEST_KEY, "value")
				.build();
		Button button = interactions.createButton(ButtonStyle.PRIMARY, "test:action", "Click", attributes);

		GenericComponentInteractionCreateEvent event = mock(GenericComponentInteractionCreateEvent.class);
		when(event.getComponentId()).thenReturn(button.getId());

		assertEquals("value", interactions.getAttributes(event).get(TEST_KEY).orElse(null));
	}

	@Test
	void unbindsByAttributeValue() {
		ComponentAttributes first = ComponentAttributes.builder()
				.put(TEST_KEY, "first")
				.build();
		ComponentAttributes second = ComponentAttributes.builder()
				.put(TEST_KEY, "second")
				.build();
		Button firstButton = interactions.createButton(ButtonStyle.PRIMARY, "test:action", "First", first);
		Button secondButton = interactions.createButton(ButtonStyle.PRIMARY, "test:action", "Second", second);

		GenericComponentInteractionCreateEvent firstEvent = mock(GenericComponentInteractionCreateEvent.class);
		when(firstEvent.getComponentId()).thenReturn(firstButton.getId());

		GenericComponentInteractionCreateEvent secondEvent = mock(GenericComponentInteractionCreateEvent.class);
		when(secondEvent.getComponentId()).thenReturn(secondButton.getId());

		interactions.unbindAllByAttribute(TEST_KEY, "first");

		assertTrue(interactions.getAttributes(firstEvent).isEmpty());
		assertEquals("second", interactions.getAttributes(secondEvent).get(TEST_KEY).orElse(null));
	}

	@Test
	void unregisterRemovesAttributeBindings() {
		ComponentAttributes attributes = ComponentAttributes.builder()
				.put(TEST_KEY, "value")
				.build();
		String componentId = "plugin:test|1";
		interactions.bindAttributes(componentId, attributes);

		GenericComponentInteractionCreateEvent event = mock(GenericComponentInteractionCreateEvent.class);
		when(event.getComponentId()).thenReturn(componentId);

		interactions.unregister("plugin");

		assertTrue(interactions.getAttributes(event).isEmpty());
	}
}
