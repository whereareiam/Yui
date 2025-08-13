package me.whereareiam.yui.api.model.requirement;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.api.type.RequirementCondition;

import java.util.List;

/**
 * Requirement for checking guild IDs (allow/deny lists).
 */
@Getter
@Setter
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
