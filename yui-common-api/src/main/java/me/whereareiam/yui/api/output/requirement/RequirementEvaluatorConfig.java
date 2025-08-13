package me.whereareiam.yui.api.output.requirement;

import me.whereareiam.yui.api.model.requirement.RequirementEntry;

import java.util.Set;

/**
 * Configuration for requirement evaluators that allows services to specify
 * which requirement types they want to utilize.
 */
public interface RequirementEvaluatorConfig {
    
    /**
     * Gets the set of requirement entry classes that this configuration supports.
     * If empty or null, all available evaluators will be used.
     * 
     * @return Set of supported requirement entry classes, or empty/null for all
     */
    Set<Class<? extends RequirementEntry>> getSupportedRequirementTypes();
    
    /**
     * Checks if this configuration supports a specific requirement entry type.
     * 
     * @param entryClass The requirement entry class to check
     * @return true if supported, false otherwise
     */
    default boolean supportsRequirementType(Class<? extends RequirementEntry> entryClass) {
        Set<Class<? extends RequirementEntry>> supported = getSupportedRequirementTypes();
        return supported == null || supported.isEmpty() || supported.contains(entryClass);
    }
    
    /**
     * Gets the name of this configuration for identification purposes.
     * 
     * @return Configuration name
     */
    String getConfigurationName();
}
