package me.whereareiam.yue.common.adapter;

import me.whereareiam.yue.api.input.ComponentIdManager;
import me.whereareiam.yue.api.model.ComponentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class StandardComponentIdManager implements ComponentIdManager {
	private static final Logger logger = LoggerFactory.getLogger(StandardComponentIdManager.class);

	// Store component info by ID
	private final Map<String, ComponentInfo> componentsById = new ConcurrentHashMap<>();

	@Override
	public String registerComponent(String moduleId, String componentType, String componentName) {
		if (moduleId == null || moduleId.isBlank()) {
			throw new IllegalArgumentException("Module ID cannot be null or blank");
		}
		if (componentType == null || componentType.isBlank()) {
			throw new IllegalArgumentException("Component type cannot be null or blank");
		}
		if (componentName == null || componentName.isBlank()) {
			throw new IllegalArgumentException("Component name cannot be null or blank");
		}

		// Create a base ID using module, type and name
		String baseId = moduleId + ":" + componentType + ":" + componentName;

		// Generate a unique ID by appending a short UUID if needed
		String componentId = baseId;
		int attempt = 0;

		while (componentsById.containsKey(componentId)) {
			// If this exact ID already exists, create a unique variant
			String shortUuid = UUID.randomUUID().toString().substring(0, 8);
			componentId = baseId + ":" + shortUuid;
			attempt++;

			if (attempt > 5) {
				// Avoid potential endless loop with a fallback
				componentId = moduleId + ":" + UUID.randomUUID().toString();
				break;
			}
		}

		// Create and store component info
		ComponentInfo info = new ComponentInfo(componentId, moduleId, componentType, componentName);
		componentsById.put(componentId, info);

		logger.debug("Registered component: {} (type: {}) for module: {}", componentName, componentType, moduleId);
		return componentId;
	}

	@Override
	public Optional<ComponentInfo> getComponentInfo(String componentId) {
		if (componentId == null || componentId.isBlank()) {
			return Optional.empty();
		}
		return Optional.ofNullable(componentsById.get(componentId));
	}

	@Override
	public Set<String> getModuleComponents(String moduleId) {
		if (moduleId == null || moduleId.isBlank()) {
			return Collections.emptySet();
		}

		return componentsById.values().stream()
				.filter(info -> info.getModuleId().equals(moduleId))
				.map(ComponentInfo::getComponentId)
				.collect(Collectors.toSet());
	}

	@Override
	public Set<String> getModuleComponentsByType(String moduleId, String componentType) {
		if (moduleId == null || moduleId.isBlank() || componentType == null || componentType.isBlank()) {
			return Collections.emptySet();
		}

		return componentsById.values().stream()
				.filter(info -> info.getModuleId().equals(moduleId) &&
						info.getComponentType().equals(componentType))
				.map(ComponentInfo::getComponentId)
				.collect(Collectors.toSet());
	}

	@Override
	public boolean unregisterComponent(String componentId) {
		if (componentId == null || componentId.isBlank()) {
			return false;
		}

		ComponentInfo removed = componentsById.remove(componentId);
		if (removed != null) {
			logger.debug("Unregistered component: {} (type: {}) for module: {}",
					removed.getComponentName(), removed.getComponentType(), removed.getModuleId());
			return true;
		}
		return false;
	}
}