package me.whereareiam.yui.api.model.requirement;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.api.type.RequirementCondition;

import java.util.List;

/**
 * Requirement for checking channel types.
 */
@Getter
@Setter
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
