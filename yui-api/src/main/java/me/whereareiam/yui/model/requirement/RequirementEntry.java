package me.whereareiam.yui.model.requirement;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.type.requirement.RequirementCondition;

/**
 * Abstract base class for requirement entries.
 * Specific requirement types should extend this class.
 */
@Getter
@Setter
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


