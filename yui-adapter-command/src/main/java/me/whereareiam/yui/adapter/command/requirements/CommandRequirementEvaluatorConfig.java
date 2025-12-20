package me.whereareiam.yui.adapter.command.requirements;

import me.whereareiam.yui.model.requirement.*;
import me.whereareiam.yui.model.requirement.type.*;
import me.whereareiam.yui.requirement.RequirementEvaluatorConfig;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Configuration for command requirement evaluators.
 * This configuration only supports requirement types that are relevant for commands.
 */
@Component
public class CommandRequirementEvaluatorConfig implements RequirementEvaluatorConfig {
	@Override
	public Set<Class<? extends RequirementEntry>> getSupportedRequirementTypes() {
		return Set.of(
				RoleRequirement.class,
				ScopeRequirement.class,
				ChannelTypeRequirement.class,
				UserRequirement.class,
				GuildRequirement.class
		);
	}

	@Override
	public String getConfigurationName() {
		return "CommandDefinition";
	}
}
