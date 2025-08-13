package me.whereareiam.yui.common.service.requirement.evaluators;

import me.whereareiam.yui.api.model.profile.UserProfile;
import me.whereareiam.yui.api.model.requirement.UserRequirement;
import me.whereareiam.yui.api.output.requirement.RequirementContext;
import me.whereareiam.yui.api.type.RequirementCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRequirementEvaluatorTest {
	private UserRequirementEvaluator evaluator;
	private UserProfile userProfile;
	private RequirementContext context;

	@BeforeEach
	void setUp() {
		evaluator = new UserRequirementEvaluator();
		userProfile = new UserProfile(12345L);
		context = new RequirementContext("test", userProfile);
	}

	@Test
	void testSupportsUserRequirement() {
		UserRequirement userReq = new UserRequirement();
		assertTrue(evaluator.supports(userReq));
	}

	@Test
	void testEvaluateWithEmptyUserIds() {
		UserRequirement userReq = new UserRequirement(RequirementCondition.HAS, true, List.of());
		assertFalse(evaluator.evaluate(context, userReq));
	}

	@Test
	void testEvaluateWithNullUserIds() {
		UserRequirement userReq = new UserRequirement();
		userReq.setUserIds(null);
		assertFalse(evaluator.evaluate(context, userReq));
	}

	@Test
	void testEvaluateHasCondition() {
		UserRequirement userReq = new UserRequirement(
				RequirementCondition.HAS,
				true,
				Arrays.asList(12345L, 67890L)
		);
		assertTrue(evaluator.evaluate(context, userReq));
	}

	@Test
	void testEvaluateHasConditionUserNotInList() {
		UserRequirement userReq = new UserRequirement(
				RequirementCondition.HAS,
				true,
				Arrays.asList(67890L, 11111L)
		);
		assertFalse(evaluator.evaluate(context, userReq));
	}

	@Test
	void testEvaluateContainsCondition() {
		UserRequirement userReq = new UserRequirement(
				RequirementCondition.CONTAINS,
				true,
				Arrays.asList(12345L, 67890L)
		);
		assertTrue(evaluator.evaluate(context, userReq));
	}

	@Test
	void testEvaluateEqualsCondition() {
		UserRequirement userReq = new UserRequirement(
				RequirementCondition.EQUALS,
				true,
				Arrays.asList(12345L)
		);
		assertTrue(evaluator.evaluate(context, userReq));
	}

	@Test
	void testEvaluateEqualsConditionMultipleUsers() {
		UserRequirement userReq = new UserRequirement(
				RequirementCondition.EQUALS,
				true,
				Arrays.asList(12345L, 67890L)
		);
		assertFalse(evaluator.evaluate(context, userReq));
	}

	@Test
	void testEvaluateGreaterThanCondition() {
		UserRequirement userReq = new UserRequirement(
				RequirementCondition.GREATER_THAN,
				true,
				Arrays.asList(10000L, 20000L)
		);
		assertTrue(evaluator.evaluate(context, userReq));
	}

	@Test
	void testEvaluateLessThanCondition() {
		UserRequirement userReq = new UserRequirement(
				RequirementCondition.LESS_THAN,
				true,
				Arrays.asList(50000L, 60000L)
		);
		assertTrue(evaluator.evaluate(context, userReq));
	}

	@Test
	void testEvaluateGreaterThanOrEqualsCondition() {
		UserRequirement userReq = new UserRequirement(
				RequirementCondition.GREATER_THAN_OR_EQUALS,
				true,
				Arrays.asList(12345L, 20000L)
		);
		assertTrue(evaluator.evaluate(context, userReq));
	}

	@Test
	void testEvaluateLessThanOrEqualsCondition() {
		UserRequirement userReq = new UserRequirement(
				RequirementCondition.LESS_THAN_OR_EQUALS,
				true,
				Arrays.asList(12345L, 20000L)
		);
		assertTrue(evaluator.evaluate(context, userReq));
	}

	@Test
	void testEvaluateWithExpectedFalse() {
		UserRequirement userReq = new UserRequirement(
				RequirementCondition.HAS,
				false,
				Arrays.asList(12345L, 67890L)
		);
		// Test raw evaluator behavior - this should return true because user is in the list
		// The expected=false logic is handled by DefaultRequirementEvaluator, not this evaluator
		assertTrue(evaluator.evaluate(context, userReq));
	}

	@Test
	void testEvaluateWithExpectedFalseUserNotInList() {
		UserRequirement userReq = new UserRequirement(
				RequirementCondition.HAS,
				false,
				Arrays.asList(67890L, 11111L)
		);
		// Test raw evaluator behavior - this should return false because user is NOT in the list
		// The expected=false logic is handled by DefaultRequirementEvaluator, not this evaluator
		assertFalse(evaluator.evaluate(context, userReq));
	}

	@Test
	void testDebugExpectedFalseLogic() {
		// Let's debug what's happening with expected=false
		UserRequirement userReq = new UserRequirement(
				RequirementCondition.HAS,
				false,
				Arrays.asList(12345L, 67890L)
		);

		// Our user ID is 12345L
		// Required user IDs are [12345L, 67890L]
		// HAS condition: user must be in the list
		// Result: user 12345L is in the list, so result = true
		// Note: This evaluator only returns the raw result, it doesn't handle expected=false logic

		boolean result = evaluator.evaluate(context, userReq);
		System.out.println("Debug: expected=false, user in list, raw result=" + result);
		assertTrue(result); // Raw evaluator should return true
	}
}
