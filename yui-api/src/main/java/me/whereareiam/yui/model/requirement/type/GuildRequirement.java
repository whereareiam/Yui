package me.whereareiam.yui.model.requirement.type;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.model.requirement.RequirementEntry;
import me.whereareiam.yui.type.requirement.RequirementCondition;

import java.util.List;

/**
 * Requirement for checking guild IDs (allow/deny lists).
 */
@Getter
@Setter
@SuppressWarnings("unused")
public class GuildRequirement extends RequirementEntry {
	private List<Long> guildIds;

	public GuildRequirement() {
		super(RequirementCondition.CONTAINS, true);
	}

	public GuildRequirement(
			RequirementCondition condition,
			boolean expected,
			List<Long> guildIds
	) {
		super(condition, expected);
		this.guildIds = guildIds;
	}
}
