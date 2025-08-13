package me.whereareiam.yui.api.model.requirement;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.api.type.RequirementCondition;

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
