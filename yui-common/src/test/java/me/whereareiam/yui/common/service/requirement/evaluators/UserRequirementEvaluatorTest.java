package me.whereareiam.yui.common.service.requirement.evaluators;

import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.model.requirement.type.UserRequirement;
import me.whereareiam.yui.model.requirement.RequirementContext;
import me.whereareiam.yui.type.requirement.RequirementCondition;
import net.dv8tion.jda.api.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRequirementEvaluatorTest {
	@Mock
	private User jdaUser;

	private UserRequirementEvaluator evaluator;
	private RequirementContext context;

	@BeforeEach
	void setUp() {
		evaluator = new UserRequirementEvaluator();
		when(jdaUser.getIdLong()).thenReturn(12345L);
		Fluctlight fluctlight = new Fluctlight(jdaUser);
		context = new RequirementContext("test", fluctlight);
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
				List.of(12345L)
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
		// Test raw evaluator behavior - this should return true because fluctlight is in the list
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
		// Test raw evaluator behavior - this should return false because fluctlight is NOT in the list
		// The expected=false logic is handled by DefaultRequirementEvaluator, not this evaluator
		assertFalse(evaluator.evaluate(context, userReq));
	}
}
