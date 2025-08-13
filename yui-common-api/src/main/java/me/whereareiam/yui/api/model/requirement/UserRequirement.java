package me.whereareiam.yui.api.model.requirement;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.api.type.RequirementCondition;

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
