package me.whereareiam.yue.api.model;

import java.time.Instant;

/**
 * Holds information about a registered interactive component.
 */
public class ComponentInfo {
	private final String componentId;
	private final String moduleId;
	private final String componentType;
	private final String componentName;
	private final Instant registrationTime;

	public ComponentInfo(String componentId, String moduleId, String componentType, String componentName) {
		this.componentId = componentId;
		this.moduleId = moduleId;
		this.componentType = componentType;
		this.componentName = componentName;
		this.registrationTime = Instant.now();
	}

	public String getComponentId() {
		return componentId;
	}

	public String getModuleId() {
		return moduleId;
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