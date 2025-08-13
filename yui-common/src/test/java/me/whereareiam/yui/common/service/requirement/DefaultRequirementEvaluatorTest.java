package me.whereareiam.yui.common.service.requirement;

import me.whereareiam.yui.api.model.profile.UserProfile;
import me.whereareiam.yui.api.model.requirement.Requirements;
import me.whereareiam.yui.api.model.requirement.UserRequirement;
import me.whereareiam.yui.api.output.requirement.RequirementContext;
import me.whereareiam.yui.api.type.RequirementCondition;
import me.whereareiam.yui.api.type.RequirementOperator;
import me.whereareiam.yui.common.service.requirement.evaluators.UserRequirementEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultRequirementEvaluatorTest {

    private DefaultRequirementEvaluator evaluator;
    private UserProfile userProfile;
    private RequirementContext context;

    @BeforeEach
    void setUp() {
        evaluator = new DefaultRequirementEvaluator(List.of(new UserRequirementEvaluator()));
        userProfile = new UserProfile(12345L);
        context = new RequirementContext("test", userProfile);
    }

    @Test
    void testEvaluateWithExpectedTrue() {
        UserRequirement userReq = new UserRequirement(
            RequirementCondition.HAS, 
            true, 
            Arrays.asList(12345L, 67890L)
        );
        
        Requirements requirements = new Requirements();
        requirements.setOperator(RequirementOperator.AND);
        requirements.getGroups().put("user", userReq);
        
        // When expected=true, requirement should pass when user is in the list
        assertTrue(evaluator.evaluate(context, requirements));
    }

    @Test
    void testEvaluateWithExpectedFalse() {
        UserRequirement userReq = new UserRequirement(
            RequirementCondition.HAS, 
            false, 
            Arrays.asList(12345L, 67890L)
        );
        
        Requirements requirements = new Requirements();
        requirements.setOperator(RequirementOperator.AND);
        requirements.getGroups().put("user", userReq);
        
        // When expected=false, requirement should pass when user is NOT in the list
        // Since user IS in the list, requirement should fail
        assertFalse(evaluator.evaluate(context, requirements));
    }

    @Test
    void testEvaluateWithExpectedFalseUserNotInList() {
        UserRequirement userReq = new UserRequirement(
            RequirementCondition.HAS, 
            false, 
            Arrays.asList(67890L, 11111L)
        );
        
        Requirements requirements = new Requirements();
        requirements.setOperator(RequirementOperator.AND);
        requirements.getGroups().put("user", userReq);
        
        // When expected=false, requirement should pass when user is NOT in the list
        // Since user is NOT in the list, requirement should pass
        assertTrue(evaluator.evaluate(context, requirements));
    }

    @Test
    void testExpectedFieldLogic() {
        // Test that the expected field logic works correctly
        UserRequirement userReq = new UserRequirement(
            RequirementCondition.HAS, 
            false, 
            Arrays.asList(12345L, 67890L)
        );
        
        Requirements requirements = new Requirements();
        requirements.setOperator(RequirementOperator.AND);
        requirements.getGroups().put("user", userReq);
        
        // The logic should be:
        // 1. UserRequirementEvaluator.evaluate() returns true (user is in list)
        // 2. DefaultRequirementEvaluator applies: expected == result -> false == true -> false
        // 3. Final result: false (requirement fails)
        
        boolean result = evaluator.evaluate(context, requirements);
        assertFalse(result);
    }
}
