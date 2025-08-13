package me.whereareiam.yui.api.model.requirement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.whereareiam.yui.api.type.RequirementOperator;

import java.util.LinkedHashMap;
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
     */
    private Map<String, RequirementEntry> groups = new LinkedHashMap<>();
}


