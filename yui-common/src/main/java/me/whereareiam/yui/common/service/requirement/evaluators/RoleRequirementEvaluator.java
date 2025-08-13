package me.whereareiam.yui.common.service.requirement.evaluators;

import me.whereareiam.yui.api.model.requirement.RoleRequirement;
import me.whereareiam.yui.api.model.requirement.RequirementEntry;
import me.whereareiam.yui.api.output.requirement.RequirementContext;
import me.whereareiam.yui.api.type.RequirementCondition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Evaluates role-based requirements.
 */
@Component
public class RoleRequirementEvaluator extends BaseRequirementEvaluator {
    
    @Override
    public boolean supports(RequirementEntry entry) {
        return entry instanceof RoleRequirement;
    }
    
    @Override
    protected boolean evaluateInternal(RequirementContext context, RequirementEntry entry) {
        RoleRequirement roleReq = (RoleRequirement) entry;
        
        if (roleReq.getRoles() == null || roleReq.getRoles().isEmpty())
            return false;
        
        List<String> requiredRoles = roleReq.getRoles();
        RequirementCondition condition = roleReq.getCondition();
        
        // Get user's roles from UserProfile context
        List<String> userRoles = extractUserRoles(context);
        
        return switch (condition) {
            case HAS -> requiredRoles.stream().allMatch(requiredRole -> 
                userRoles.stream().anyMatch(userRole -> userRole.equals(requiredRole))); // User must have ALL required roles
            case CONTAINS -> userRoles.stream().anyMatch(userRole -> 
                requiredRoles.stream().anyMatch(reqRole -> userRole.equals(reqRole))); // User must have AT LEAST ONE required role
            case EQUALS -> userRoles.stream().anyMatch(requiredRoles::contains); // User must have AT LEAST ONE required role (same as CONTAINS)
            case GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUALS, LESS_THAN_OR_EQUALS -> false; // Not applicable for roles
        };
    }
    
    /**
     * Extracts user roles from the UserProfile context.
     * 
     * @param context The context object containing UserProfile
     * @return List of user role identifiers
     */
    private List<String> extractUserRoles(RequirementContext context) {
        if (context.getUserProfile().getRoles() == null) {
            return List.of();
        }
        
        return Arrays.stream(context.getUserProfile().getRoles())
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());
    }
}
