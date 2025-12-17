package me.whereareiam.yui.model.requirement.type;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.model.requirement.RequirementEntry;
import me.whereareiam.yui.type.requirement.RequirementCondition;

import java.util.List;

/**
 * Requirement for checking user IDs (allow/deny lists).
 */
@Getter
@Setter
public class UserRequirement extends RequirementEntry {
	private List<Long> userIds;

	public UserRequirement() {
		super(RequirementCondition.CONTAINS, true);
	}

	public UserRequirement(
			RequirementCondition condition,
			Boolean expected,
			List<Long> userIds
	) {
		super(condition, expected);
		this.userIds = userIds;
	}
}
