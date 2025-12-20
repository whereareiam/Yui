package me.whereareiam.yui.common.service.requirement.evaluators;

import me.whereareiam.yui.model.requirement.RequirementEntry;
import me.whereareiam.yui.model.requirement.type.UserRequirement;
import me.whereareiam.yui.model.requirement.RequirementContext;
import me.whereareiam.yui.type.requirement.RequirementCondition;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Evaluates fluctlight requirements.
 */
@Component
public class UserRequirementEvaluator extends BaseRequirementEvaluator {
	@Override
	public boolean supports(RequirementEntry entry) {
		return entry instanceof UserRequirement;
	}

	@Override
	protected boolean evaluateInternal(RequirementContext context, RequirementEntry entry) {
		UserRequirement userReq = (UserRequirement) entry;

		if (userReq.getUserIds() == null || userReq.getUserIds().isEmpty())
			return false;

		List<Long> requiredUserIds = userReq.getUserIds();
		RequirementCondition condition = userReq.getCondition();

		// Get current fluctlight ID from UserProfile context
		Long currentUserId = context.getUserId();

		return switch (condition) {
			case HAS -> requiredUserIds.contains(currentUserId); // User must be in the list
			case CONTAINS ->
					requiredUserIds.contains(currentUserId); // User must be in the list (same as HAS for single fluctlight)
			case EQUALS ->
					requiredUserIds.size() == 1 && requiredUserIds.contains(currentUserId); // User must be the only one in the list
			case GREATER_THAN -> requiredUserIds.stream().anyMatch(id -> currentUserId > id);
			case LESS_THAN -> requiredUserIds.stream().anyMatch(id -> currentUserId < id);
			case GREATER_THAN_OR_EQUALS -> requiredUserIds.stream().anyMatch(id -> currentUserId >= id);
			case LESS_THAN_OR_EQUALS -> requiredUserIds.stream().anyMatch(id -> currentUserId <= id);
		};
	}
}
