package me.whereareiam.yue.api.input;

import me.whereareiam.yue.api.model.ComponentInfo;

import java.util.Optional;
import java.util.Set;

/**
 * Manages component IDs across the application to ensure uniqueness
 * and provide a central registry for all interactive components.
 */
public interface ComponentIdManager {
	/**
	 * Component type constants
	 */
	String TYPE_BUTTON = "button";
	String TYPE_SELECT_MENU = "select_menu";
	String TYPE_MODAL = "modal";
	String TYPE_TEXT_INPUT = "text_input";

	/**
	 * Registers a component with a specific plugin.
	 *
	 * @param pluginId      Identifier of the plugin registering the component
	 * @param componentType Type of the component (button, select, modal, etc.)
	 * @param componentName Logical name of the component within the plugin
	 * @return The globally unique component ID to use
	 */
	String registerComponent(String pluginId, String componentType, String componentName);

	/**
	 * Convenience method to register a button component.
	 */
	default String registerButton(String pluginId, String buttonName) {
		return registerComponent(pluginId, TYPE_BUTTON, buttonName);
	}

	/**
	 * Convenience method to register a select menu component.
	 */
	default String registerSelectMenu(String pluginId, String menuName) {
		return registerComponent(pluginId, TYPE_SELECT_MENU, menuName);
	}

	/**
	 * Convenience method to register a modal component.
	 */
	default String registerModal(String pluginId, String modalName) {
		return registerComponent(pluginId, TYPE_MODAL, modalName);
	}

	/**
	 * Convenience method to register a text input component.
	 */
	default String registerTextInput(String pluginId, String textInputName) {
		return registerComponent(pluginId, TYPE_TEXT_INPUT, textInputName);
	}

	/**
	 * Retrieves information about a component from its ID.
	 */
	Optional<ComponentInfo> getComponentInfo(String componentId);

	/**
	 * Lists all registered component IDs for a specific plugin.
	 */
	Set<String> getPluginComponents(String pluginId);

	/**
	 * Lists all registered component IDs for a specific plugin and component type.
	 */
	Set<String> getPluginComponentsByType(String pluginId, String componentType);

	/**
	 * Removes a component ID registration.
	 */
	boolean unregisterComponent(String componentId);
}