package me.whereareiam.yui.common.requirement;

import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.model.requirement.Requirements;
import me.whereareiam.yui.model.requirement.type.UserRequirement;
import me.whereareiam.yui.model.requirement.RequirementContext;
import me.whereareiam.yui.type.requirement.RequirementCondition;
import me.whereareiam.yui.type.requirement.RequirementOperator;
import me.whereareiam.yui.common.requirement.evaluators.UserRequirementEvaluator;
import net.dv8tion.jda.api.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultRequirementEvaluatorTest {
	@Mock
	private User jdaUser;

    private DefaultRequirementEvaluator evaluator;
	private RequirementContext context;

    @BeforeEach
    void setUp() {
        evaluator = new DefaultRequirementEvaluator(List.of(new UserRequirementEvaluator()));
		when(jdaUser.getIdLong()).thenReturn(12345L);
		Fluctlight fluctlight = new Fluctlight(jdaUser);
        context = new RequirementContext("test", fluctlight);
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
        requirements.setGroups(Map.of("user", userReq));
        
        // When expected=true, requirement should pass when fluctlight is in the list
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
        requirements.setGroups(Map.of("user", userReq));
        
        // When expected=false, requirement should pass when fluctlight is NOT in the list
        // Since fluctlight IS in the list, requirement should fail
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
        requirements.setGroups(Map.of("user", userReq));
        
        // When expected=false, requirement should pass when fluctlight is NOT in the list
        // Since fluctlight is NOT in the list, requirement should pass
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
        requirements.setGroups(Map.of("user", userReq));
        
        // The logic should be:
        // 1. UserRequirementEvaluator.evaluate() returns true (fluctlight is in list)
        // 2. DefaultRequirementEvaluator applies: expected == result -> false == true -> false
        // 3. Final result: false (requirement fails)
        
        boolean result = evaluator.evaluate(context, requirements);
        assertFalse(result);
    }
}
