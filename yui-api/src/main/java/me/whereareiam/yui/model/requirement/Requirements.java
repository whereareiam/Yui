package me.whereareiam.yui.model.requirement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.whereareiam.configura.annotation.Field;
import me.whereareiam.configura.annotation.MergeStrategy;
import me.whereareiam.yui.type.requirement.RequirementOperator;

import java.util.Map;

/**
 * Global requirement definition container; reusable by commands and other features.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Requirements {
    private RequirementOperator operator;
    /**
     * Flat map of requirement entries. The key is an arbitrary name; the value describes a single requirement.
     * Uses SHALLOW merge strategy so users can permanently delete requirement groups
     * without them reappearing from templates.
     */
    @Field(merge = MergeStrategy.SHALLOW)
    private Map<String, RequirementEntry> groups;
}


