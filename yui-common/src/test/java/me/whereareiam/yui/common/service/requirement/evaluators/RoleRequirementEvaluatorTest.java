package me.whereareiam.yui.common.service.requirement.evaluators;

import me.whereareiam.yui.api.model.profile.UserProfile;
import me.whereareiam.yui.api.model.requirement.RoleRequirement;
import me.whereareiam.yui.api.output.requirement.RequirementContext;
import me.whereareiam.yui.api.type.RequirementCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleRequirementEvaluatorTest {
	private RoleRequirementEvaluator evaluator;
	private UserProfile userProfile;
	private RequirementContext context;

	@BeforeEach
	void setUp() {
		evaluator = new RoleRequirementEvaluator();
		userProfile = new UserProfile(12345L);
		userProfile.setRoles(new long[]{100L, 200L, 300L});
		context = new RequirementContext("test", userProfile);
	}

	@Test
	void testSupportsRoleRequirement() {
		RoleRequirement roleReq = new RoleRequirement();
		assertTrue(evaluator.supports(roleReq));
	}

	@Test
	void testEvaluateWithEmptyRoles() {
		RoleRequirement roleReq = new RoleRequirement(RequirementCondition.HAS, true, List.of(), "ID");
		assertFalse(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateWithNullRoles() {
		RoleRequirement roleReq = new RoleRequirement();
		roleReq.setRoles(null);
		assertFalse(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateHasCondition() {
		RoleRequirement roleReq = new RoleRequirement(
				RequirementCondition.HAS,
				true,
				Arrays.asList("100", "200"),
				"ID"
		);
		assertTrue(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateHasConditionUserMissingOneRole() {
		RoleRequirement roleReq = new RoleRequirement(
				RequirementCondition.HAS,
				true,
				Arrays.asList("100", "200", "400"),
				"ID"
		);
		assertFalse(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateContainsCondition() {
		RoleRequirement roleReq = new RoleRequirement(
				RequirementCondition.CONTAINS,
				true,
				Arrays.asList("100", "400", "500"),
				"ID"
		);
		assertTrue(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateContainsConditionUserHasNoRoles() {
		RoleRequirement roleReq = new RoleRequirement(
				RequirementCondition.CONTAINS,
				true,
				Arrays.asList("400", "500", "600"),
				"ID"
		);
		assertFalse(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateEqualsCondition() {
		RoleRequirement roleReq = new RoleRequirement(
				RequirementCondition.EQUALS,
				true,
				Arrays.asList("100", "400", "500"),
				"ID"
		);
		assertTrue(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateWithUserProfileNoRoles() {
		UserProfile noRolesProfile = new UserProfile(12345L);
		noRolesProfile.setRoles(null);
		RequirementContext noRolesContext = new RequirementContext("test", noRolesProfile);

		RoleRequirement roleReq = new RoleRequirement(
				RequirementCondition.HAS,
				true,
				Arrays.asList("100", "200", "300"),
				"ID"
		);
		assertFalse(evaluator.evaluate(noRolesContext, roleReq));
	}

	@Test
	void testEvaluateWithExpectedFalse() {
		RoleRequirement roleReq = new RoleRequirement(
			RequirementCondition.HAS, 
			false, 
			Arrays.asList("100", "200"),
			"ID"
		);
		// Test raw evaluator behavior - this should return true because user has all required roles
		// The expected=false logic is handled by DefaultRequirementEvaluator, not this evaluator
		assertTrue(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateWithExpectedFalseUserMissingRoles() {
		RoleRequirement roleReq = new RoleRequirement(
			RequirementCondition.HAS, 
			false, 
			Arrays.asList("100", "200", "400"),
			"ID"
		);
		// Test raw evaluator behavior - this should return false because user is missing role 400
		// The expected=false logic is handled by DefaultRequirementEvaluator, not this evaluator
		assertFalse(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testDebugExpectedFalseLogic() {
		// Let's debug what's happening with expected=false
		RoleRequirement roleReq = new RoleRequirement(
			RequirementCondition.HAS, 
			false, 
			Arrays.asList("100", "200"),
			"ID"
		);
		
		// Our user has roles [100, 200, 300]
		// Required roles are ["100", "200"] 
		// HAS condition: user must have ALL required roles
		// Result: user has both 100 and 200, so result = true
		// Note: This evaluator only returns the raw result, it doesn't handle expected=false logic
		
		boolean result = evaluator.evaluate(context, roleReq);
		System.out.println("Debug: expected=false, user has all required roles, raw result=" + result);
		assertTrue(result); // Raw evaluator should return true
	}

	@Test
	void testEvaluateWithNullRoleMatchBy() {
		RoleRequirement roleReq = new RoleRequirement(
				RequirementCondition.HAS,
				true,
				Arrays.asList("100", "200"),
				null
		);
		// Should default to ID matching when roleMatchBy is null
		assertTrue(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateWithEmptyRoleMatchBy() {
		RoleRequirement roleReq = new RoleRequirement(
				RequirementCondition.HAS,
				true,
				Arrays.asList("100", "200"),
				""
		);
		// Should default to ID matching when roleMatchBy is empty
		assertTrue(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateWithInvalidRoleMatchBy() {
		RoleRequirement roleReq = new RoleRequirement(
				RequirementCondition.HAS,
				true,
				Arrays.asList("100", "200"),
				"INVALID"
		);
		// Should fallback to ID matching for unknown match types
		assertTrue(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateWithCaseInsensitiveRoleMatchBy() {
		RoleRequirement roleReq = new RoleRequirement(
				RequirementCondition.HAS,
				true,
				Arrays.asList("100", "200"),
				"id"
		);
		// Should work with lowercase "id"
		assertTrue(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateWithNameRoleMatchBy() {
		RoleRequirement roleReq = new RoleRequirement(
				RequirementCondition.HAS,
				true,
				Arrays.asList("Admin", "Moderator"),
				"NAME"
		);
		// When matching by NAME, it will try to extract from context
		// Since our test context has no original context, it should return empty list
		// and the evaluation should fail
		assertFalse(evaluator.evaluate(context, roleReq));
	}
}
