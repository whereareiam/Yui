package me.whereareiam.yue.common.adapter;

import me.whereareiam.yue.api.model.ComponentInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ComponentIdManagerAdapterTest {
	private ComponentIdManagerAdapter componentIdManager;

	@BeforeEach
	void setUp() {
		componentIdManager = new ComponentIdManagerAdapter();
	}

	@Test
	void registerComponent_withValidInput_shouldReturnComponentId() {
		// Arrange
		String pluginId = "test-plugin";
		String componentType = "button";
		String componentName = "test-button";

		// Act
		String componentId = componentIdManager.registerComponent(pluginId, componentType, componentName);

		// Assert
		assertNotNull(componentId);
		assertTrue(componentId.contains(pluginId));
		assertTrue(componentId.contains(componentType));
		assertTrue(componentId.contains(componentName));
	}

	@Test
	void registerComponent_withInvalidInput_shouldThrowException() {
		// Assert
		assertThrows(IllegalArgumentException.class, () ->
				componentIdManager.registerComponent("", "type", "name"));
		assertThrows(IllegalArgumentException.class, () ->
				componentIdManager.registerComponent("plugin", "", "name"));
		assertThrows(IllegalArgumentException.class, () ->
				componentIdManager.registerComponent("plugin", "type", ""));
	}

	@Test
	void getComponentInfo_withExistingId_shouldReturnInfo() {
		// Arrange
		String pluginId = "test-plugin";
		String componentType = "button";
		String componentName = "test-button";
		String componentId = componentIdManager.registerComponent(pluginId, componentType, componentName);

		// Act
		Optional<ComponentInfo> result = componentIdManager.getComponentInfo(componentId);

		// Assert
		assertTrue(result.isPresent());
		assertEquals(componentId, result.get().getComponentId());
		assertEquals(pluginId, result.get().getPluginId());
		assertEquals(componentType, result.get().getComponentType());
		assertEquals(componentName, result.get().getComponentName());
	}

	@Test
	void getComponentInfo_withNonExistingId_shouldReturnEmpty() {
		// Act
		Optional<ComponentInfo> result = componentIdManager.getComponentInfo("non-existing");

		// Assert
		assertTrue(result.isEmpty());
	}

	@Test
	void getPluginComponents_withExistingPlugin_shouldReturnComponents() {
		// Arrange
		String pluginId = "test-plugin";
		componentIdManager.registerComponent(pluginId, "button", "button1");
		componentIdManager.registerComponent(pluginId, "select", "select1");

		// Act
		Set<String> result = componentIdManager.getPluginComponents(pluginId);

		// Assert
		assertEquals(2, result.size());
	}

	@Test
	void getPluginComponentsByType_withValidInput_shouldReturnFilteredComponents() {
		// Arrange
		String pluginId = "test-plugin";
		componentIdManager.registerComponent(pluginId, "button", "button1");
		componentIdManager.registerComponent(pluginId, "button", "button2");
		componentIdManager.registerComponent(pluginId, "select", "select1");

		// Act
		Set<String> result = componentIdManager.getPluginComponentsByType(pluginId, "button");

		// Assert
		assertEquals(2, result.size());
	}

	@Test
	void unregisterComponent_withExistingId_shouldRemoveAndReturnTrue() {
		// Arrange
		String componentId = componentIdManager.registerComponent("plugin", "type", "name");

		// Act
		boolean result = componentIdManager.unregisterComponent(componentId);

		// Assert
		assertTrue(result);
		assertTrue(componentIdManager.getComponentInfo(componentId).isEmpty());
	}

	@Test
	void unregisterComponent_withNonExistingId_shouldReturnFalse() {
		// Act
		boolean result = componentIdManager.unregisterComponent("non-existing");

		// Assert
		assertFalse(result);
	}
}