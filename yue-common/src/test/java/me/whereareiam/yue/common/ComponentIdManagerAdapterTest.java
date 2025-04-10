package me.whereareiam.yue.common;

import me.whereareiam.yue.api.model.ComponentInfo;
import me.whereareiam.yue.common.adapter.ComponentIdManagerAdapter;
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
		String moduleId = "test-module";
		String componentType = "button";
		String componentName = "test-button";

		// Act
		String componentId = componentIdManager.registerComponent(moduleId, componentType, componentName);

		// Assert
		assertNotNull(componentId);
		assertTrue(componentId.contains(moduleId));
		assertTrue(componentId.contains(componentType));
		assertTrue(componentId.contains(componentName));
	}

	@Test
	void registerComponent_withInvalidInput_shouldThrowException() {
		// Assert
		assertThrows(IllegalArgumentException.class, () ->
				componentIdManager.registerComponent("", "type", "name"));
		assertThrows(IllegalArgumentException.class, () ->
				componentIdManager.registerComponent("module", "", "name"));
		assertThrows(IllegalArgumentException.class, () ->
				componentIdManager.registerComponent("module", "type", ""));
	}

	@Test
	void getComponentInfo_withExistingId_shouldReturnInfo() {
		// Arrange
		String moduleId = "test-module";
		String componentType = "button";
		String componentName = "test-button";
		String componentId = componentIdManager.registerComponent(moduleId, componentType, componentName);

		// Act
		Optional<ComponentInfo> result = componentIdManager.getComponentInfo(componentId);

		// Assert
		assertTrue(result.isPresent());
		assertEquals(componentId, result.get().getComponentId());
		assertEquals(moduleId, result.get().getModuleId());
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
	void getModuleComponents_withExistingModule_shouldReturnComponents() {
		// Arrange
		String moduleId = "test-module";
		componentIdManager.registerComponent(moduleId, "button", "button1");
		componentIdManager.registerComponent(moduleId, "select", "select1");

		// Act
		Set<String> result = componentIdManager.getModuleComponents(moduleId);

		// Assert
		assertEquals(2, result.size());
	}

	@Test
	void getModuleComponentsByType_withValidInput_shouldReturnFilteredComponents() {
		// Arrange
		String moduleId = "test-module";
		componentIdManager.registerComponent(moduleId, "button", "button1");
		componentIdManager.registerComponent(moduleId, "button", "button2");
		componentIdManager.registerComponent(moduleId, "select", "select1");

		// Act
		Set<String> result = componentIdManager.getModuleComponentsByType(moduleId, "button");

		// Assert
		assertEquals(2, result.size());
	}

	@Test
	void unregisterComponent_withExistingId_shouldRemoveAndReturnTrue() {
		// Arrange
		String componentId = componentIdManager.registerComponent("module", "type", "name");

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