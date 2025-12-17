package me.whereareiam.yui.common.service.requirement.config;

import me.whereareiam.yui.model.requirement.RequirementEntry;
import me.whereareiam.yui.requirement.RequirementEvaluatorConfig;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Default configuration that supports all requirement types.
 * This is used when no specific configuration is provided.
 */
@Component
public class DefaultRequirementEvaluatorConfig implements RequirementEvaluatorConfig {
	@Override
	public Set<Class<? extends RequirementEntry>> getSupportedRequirementTypes() {
		// Return null to indicate support for all types
		return null;
	}

	@Override
	public String getConfigurationName() {
		return "Default";
	}
}
