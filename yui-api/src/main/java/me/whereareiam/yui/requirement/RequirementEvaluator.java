package me.whereareiam.yui.requirement;

import me.whereareiam.yui.model.requirement.RequirementContext;
import me.whereareiam.yui.model.requirement.Requirements;

/**
 * Core interface for evaluating command requirements.
 * This is module-agnostic and can be implemented by different adapters.
 */
public interface RequirementEvaluator {
    /**
     * Evaluates requirements against a given context that includes UserProfile.
     * 
     * @param context The context object containing UserProfile and original context
     * @param requirements The requirements to evaluate
     * @return true if all requirements are met, false otherwise
     */
    boolean evaluate(RequirementContext context, Requirements requirements);
    
    /**
     * Evaluates requirements against a given context with specific configuration.
     * 
     * @param context The context object containing UserProfile and original context
     * @param requirements The requirements to evaluate
     * @param config Configuration specifying which requirement types to use (null for all)
     * @return true if all requirements are met, false otherwise
     */
    boolean evaluate(RequirementContext context, Requirements requirements, RequirementEvaluatorConfig config);
}
