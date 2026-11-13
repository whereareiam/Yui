package me.whereareiam.yui.model.requirement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.whereareiam.configura.annotation.merge.Merge;
import me.whereareiam.configura.type.merge.WhenPresent;
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
    @Merge(present = WhenPresent.KEEP_AS_WRITTEN)
    private Map<String, RequirementEntry> groups;
}


