package me.whereareiam.yui.model.requirement.type;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.model.requirement.RequirementEntry;
import me.whereareiam.yui.type.requirement.RequirementCondition;

import java.util.List;

/**
 * Requirement for checking channel types.
 */
@Getter
@Setter
@SuppressWarnings("unused")
public class ChannelTypeRequirement extends RequirementEntry {
	private List<String> types;

	public ChannelTypeRequirement() {
		super(RequirementCondition.CONTAINS, true);
	}

	public ChannelTypeRequirement(
			RequirementCondition condition,
			boolean expected,
			List<String> types
	) {
		super(condition, expected);
		this.types = types;
	}
}
