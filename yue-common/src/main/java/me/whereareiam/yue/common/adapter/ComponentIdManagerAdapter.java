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
public class ComponentIdManagerAdapter implements ComponentIdManager {
	private static final Logger logger = LoggerFactory.getLogger(ComponentIdManagerAdapter.class);

	private final Map<String, ComponentInfo> componentsById = new ConcurrentHashMap<>();

	@Override
	public String registerComponent(String pluginId, String componentType, String componentName) {
		if (pluginId == null || pluginId.isBlank())
			throw new IllegalArgumentException("YuePluginDescriptor ID cannot be null or blank");
		if (componentType == null || componentType.isBlank())
			throw new IllegalArgumentException("Component type cannot be null or blank");
		if (componentName == null || componentName.isBlank())
			throw new IllegalArgumentException("Component name cannot be null or blank");

		String baseId = pluginId + ":" + componentType + ":" + componentName;

		String componentId = baseId;
		int attempt = 0;

		while (componentsById.containsKey(componentId)) {
			String shortUuid = UUID.randomUUID().toString().substring(0, 8);
			componentId = baseId + ":" + shortUuid;
			attempt++;

			if (attempt > 5) {
				componentId = pluginId + ":" + UUID.randomUUID();
				break;
			}
		}

		ComponentInfo info = new ComponentInfo(componentId, pluginId, componentType, componentName);
		componentsById.put(componentId, info);

		logger.debug("Registered component: {} (type: {}) for plugin: {}", componentName, componentType, pluginId);
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
	public Set<String> getPluginComponents(String pluginId) {
		if (pluginId == null || pluginId.isBlank()) {
			return Collections.emptySet();
		}

		return componentsById.values().stream()
				.filter(info -> info.getPluginId().equals(pluginId))
				.map(ComponentInfo::getComponentId)
				.collect(Collectors.toSet());
	}

	@Override
	public Set<String> getPluginComponentsByType(String pluginId, String componentType) {
		if (pluginId == null || pluginId.isBlank() || componentType == null || componentType.isBlank()) {
			return Collections.emptySet();
		}

		return componentsById.values().stream()
				.filter(info -> info.getPluginId().equals(pluginId) &&
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
			logger.debug("Unregistered component: {} (type: {}) for plugin: {}",
					removed.getComponentName(), removed.getComponentType(), removed.getPluginId());
			return true;
		}
		return false;
	}
}