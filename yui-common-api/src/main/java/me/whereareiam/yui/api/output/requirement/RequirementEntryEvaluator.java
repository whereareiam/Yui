package me.whereareiam.yui.api.output.requirement;

import me.whereareiam.yui.api.model.requirement.RequirementEntry;

/**
 * Interface for evaluating individual requirement entries.
 * This is module-agnostic and can be implemented by different adapters.
 */
public interface RequirementEntryEvaluator {
    /**
     * Checks if this evaluator supports the given requirement entry type.
     * 
     * @param entry The requirement entry to check support for
     * @return true if this evaluator can handle the entry type
     */
    boolean supports(RequirementEntry entry);
    
    /**
     * Evaluates a single requirement entry against the given context.
     * 
     * @param context The context object containing UserProfile and original context
     * @param entry The requirement entry to evaluate
     * @return true if the requirement is met, false otherwise
     */
    boolean evaluate(RequirementContext context, RequirementEntry entry);
}
