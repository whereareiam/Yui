package me.whereareiam.yui.model.requirement;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.configura.annotation.Polymorphic;
import me.whereareiam.yui.model.requirement.type.ChannelTypeRequirement;
import me.whereareiam.yui.model.requirement.type.GuildRequirement;
import me.whereareiam.yui.model.requirement.type.RoleRequirement;
import me.whereareiam.yui.model.requirement.type.ScopeRequirement;
import me.whereareiam.yui.model.requirement.type.UserRequirement;
import me.whereareiam.yui.type.requirement.RequirementCondition;

/**
 * Abstract base class for requirement entries.
 * Specific requirement types should extend this class.
 */
@Getter
@Setter
@Polymorphic(
		inferBy = {
				@Polymorphic.Infer(field = "roles", target = RoleRequirement.class),
				@Polymorphic.Infer(field = "userIds", target = UserRequirement.class),
				@Polymorphic.Infer(field = "guildIds", target = GuildRequirement.class),
				@Polymorphic.Infer(field = "types", target = ChannelTypeRequirement.class),
				@Polymorphic.Infer(field = "scopes", target = ScopeRequirement.class)
		}
)
public abstract class RequirementEntry {
	private RequirementCondition condition;
	private Boolean expected; // default true when null

	protected RequirementEntry(
			RequirementCondition condition,
			Boolean expected
	) {
		this.condition = condition;
		this.expected = expected;
	}

	protected RequirementEntry() {
		// For serialization
	}
}


