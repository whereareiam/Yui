package me.whereareiam.yui.model.requirement.type;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.model.requirement.RequirementEntry;
import me.whereareiam.yui.type.requirement.RequirementCondition;

import java.util.List;

/**
 * Requirement for checking fluctlight roles.
 */
@Getter
@Setter
public class RoleRequirement extends RequirementEntry {
	private List<String> roles;
	private String roleMatchBy; // ID | NAME

	public RoleRequirement() {
		super(RequirementCondition.HAS, true);
	}

	public RoleRequirement(
			RequirementCondition condition,
			boolean expected,
			List<String> roles,
			String roleMatchBy
	) {
		super(condition, expected);
		this.roles = roles;
		this.roleMatchBy = roleMatchBy;
	}
}
