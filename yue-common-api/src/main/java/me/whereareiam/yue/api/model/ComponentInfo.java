package me.whereareiam.yue.api.model;

import java.time.Instant;

/**
 * Holds information about a registered interactive component.
 */
public class ComponentInfo {
	private final String componentId;
	private final String pluginId;
	private final String componentType;
	private final String componentName;
	private final Instant registrationTime;

	public ComponentInfo(String componentId, String pluginId, String componentType, String componentName) {
		this.componentId = componentId;
		this.pluginId = pluginId;
		this.componentType = componentType;
		this.componentName = componentName;
		this.registrationTime = Instant.now();
	}

	public String getComponentId() {
		return componentId;
	}

	public String getPluginId() {
		return pluginId;
	}

	public String getComponentType() {
		return componentType;
	}

	public String getComponentName() {
		return componentName;
	}

	public Instant getRegistrationTime() {
		return registrationTime;
	}
}