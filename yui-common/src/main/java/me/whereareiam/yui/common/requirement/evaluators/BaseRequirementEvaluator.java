package me.whereareiam.yui.common.requirement.evaluators;

import me.whereareiam.yui.model.requirement.RequirementEntry;
import me.whereareiam.yui.model.requirement.RequirementContext;
import me.whereareiam.yui.requirement.RequirementEntryEvaluator;

/**
 * Base class for requirement evaluators that provides common functionality.
 * This allows for easier extension and consistent behavior across evaluators.
 */
public abstract class BaseRequirementEvaluator implements RequirementEntryEvaluator {
    
    /**
     * Default implementation that checks if the context is valid for evaluation.
     * Subclasses can override this to provide more specific validation.
     */
    @Override
    public boolean evaluate(RequirementContext context, RequirementEntry entry) {
        if (context == null || entry == null)
            return false;
        
        if (!supports(entry))
            return false;
        
        return evaluateInternal(context, entry);
    }
    
    /**
     * Internal evaluation method that subclasses must implement.
     * This method is called after basic validation.
     */
    protected abstract boolean evaluateInternal(RequirementContext context, RequirementEntry entry);
}
