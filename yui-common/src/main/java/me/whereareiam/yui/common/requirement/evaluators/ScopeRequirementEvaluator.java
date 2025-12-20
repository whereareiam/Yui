package me.whereareiam.yui.common.requirement.evaluators;

import me.whereareiam.yui.model.requirement.RequirementEntry;
import me.whereareiam.yui.model.requirement.type.ScopeRequirement;
import me.whereareiam.yui.model.requirement.RequirementContext;
import me.whereareiam.yui.type.requirement.RequirementCondition;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Evaluates scope requirements.
 */
@Component
public class ScopeRequirementEvaluator extends BaseRequirementEvaluator {
	@Override
	public boolean supports(RequirementEntry entry) {
		return entry instanceof ScopeRequirement;
	}

	@Override
	protected boolean evaluateInternal(RequirementContext context, RequirementEntry entry) {
		ScopeRequirement scopeReq = (ScopeRequirement) entry;

		if (scopeReq.getScopes() == null || scopeReq.getScopes().isEmpty())
			return false;

		List<String> requiredScopes = scopeReq.getScopes();
		RequirementCondition condition = scopeReq.getCondition();

		// Get current scope from original context
		String currentScope = extractCurrentScope(context);

		return switch (condition) {
			case HAS -> requiredScopes.contains(currentScope);
			case CONTAINS -> requiredScopes.contains(currentScope);
			case EQUALS -> requiredScopes.contains(currentScope);
			case GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUALS, LESS_THAN_OR_EQUALS ->
					false; // Not applicable for scopes
		};
	}

	/**
	 * Extracts the current scope from the original context.
	 *
	 * @param context The context object containing UserProfile and original context
	 * @return Current scope identifier
	 */
	private String extractCurrentScope(RequirementContext context) {
		Object originalContext = context.getOriginalContext();
		
		if (originalContext instanceof SlashCommandInteractionEvent event) {
			return event.getChannelType().name();
		} else if (originalContext instanceof ButtonInteractionEvent event) {
			return event.getChannelType().name();
		} else if (originalContext instanceof StringSelectInteractionEvent event) {
			return event.getChannelType().name();
		}
		
		return "UNKNOWN";
	}
}
