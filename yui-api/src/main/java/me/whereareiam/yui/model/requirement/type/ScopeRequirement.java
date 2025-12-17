package me.whereareiam.yui.model.requirement.type;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.model.requirement.RequirementEntry;
import me.whereareiam.yui.type.requirement.RequirementCondition;

import java.util.List;

/**
 * Requirement for checking context scope (GUILD, DM).
 */
@Getter
@Setter
public class ScopeRequirement extends RequirementEntry {
	private List<String> scopes;

	public ScopeRequirement() {
		super(RequirementCondition.CONTAINS, true);
	}

	public ScopeRequirement(
			RequirementCondition condition,
			boolean expected,
			List<String> scopes
	) {
		super(condition, expected);
		this.scopes = scopes;
	}
}
