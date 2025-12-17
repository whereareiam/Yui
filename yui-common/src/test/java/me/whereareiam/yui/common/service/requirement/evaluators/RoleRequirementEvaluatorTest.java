package me.whereareiam.yui.common.service.requirement.evaluators;

import me.whereareiam.yui.model.profile.UserProfile;
import me.whereareiam.yui.model.requirement.type.RoleRequirement;
import me.whereareiam.yui.model.requirement.RequirementContext;
import me.whereareiam.yui.type.requirement.RequirementCondition;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class RoleRequirementEvaluatorTest {
	private RoleRequirementEvaluator evaluator;
	private UserProfile userProfile;
	private RequirementContext context;
	
	@Mock private SlashCommandInteractionEvent mockEvent;
	@Mock private Guild mockGuild;
	@Mock private User mockUser;
	@Mock private Member mockMember;
	@Mock private Role role100;
	@Mock private Role role200;
	@Mock private Role role300;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		evaluator = new RoleRequirementEvaluator();
		
		// Setup mock user profile
		userProfile = new UserProfile(12345L);
		userProfile.setRoles(new long[]{100L, 200L, 300L});
		
		// Setup mock Discord event with roles
		setupMockDiscordEvent();
		
		// Create context with mock Discord event
		context = new RequirementContext(mockEvent, userProfile);
	}
	
	private void setupMockDiscordEvent() {
		// Setup mock roles
		when(role100.getIdLong()).thenReturn(100L);
		when(role100.getName()).thenReturn("Role100");
		when(role200.getIdLong()).thenReturn(200L);
		when(role200.getName()).thenReturn("Role200");
		when(role300.getIdLong()).thenReturn(300L);
		when(role300.getName()).thenReturn("Role300");
		
		// Setup mock member with roles
		when(mockMember.getRoles()).thenReturn(Arrays.asList(role100, role200, role300));
		
		// Setup mock event
		when(mockEvent.getMember()).thenReturn(mockMember);
		when(mockEvent.getUser()).thenReturn(mockUser);
		when(mockEvent.getGuild()).thenReturn(mockGuild);
		when(mockUser.getIdLong()).thenReturn(12345L);
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
				Arrays.asList("Role100", "Role200"),
				"NAME"
		);
		// When matching by NAME, it extracts from Discord member context
		// Our mock member has roles [Role100, Role200, Role300]
		// Required roles are ["Role100", "Role200"] 
		// HAS condition: user must have ALL required roles
		// Result: user has both Role100 and Role200, so result = true
		assertTrue(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateWithNameRoleMatchByMissingRole() {
		RoleRequirement roleReq = new RoleRequirement(
				RequirementCondition.HAS,
				true,
				Arrays.asList("Role100", "Role400"),
				"NAME"
		);
		// When matching by NAME, it extracts from Discord member context
		// Our mock member has roles [Role100, Role200, Role300]
		// Required roles are ["Role100", "Role400"] 
		// HAS condition: user must have ALL required roles
		// Result: user is missing Role400, so result = false
		assertFalse(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateWithNoDiscordContext() {
		// Create context without Discord event (fallback to user profile)
		RequirementContext noDiscordContext = new RequirementContext("test", userProfile);
		
		RoleRequirement roleReq = new RoleRequirement(
				RequirementCondition.HAS,
				true,
				Arrays.asList("100", "200"),
				"ID"
		);
		// When there's no Discord context, it falls back to user profile
		// Our user profile has roles [100, 200, 300]
		// Required roles are ["100", "200"] 
		// HAS condition: user must have ALL required roles
		// Result: user has both 100 and 200, so result = true
		assertTrue(evaluator.evaluate(noDiscordContext, roleReq));
	}

	@Test
	void testEvaluateWithNoDiscordContextNameMatching() {
		// Create context without Discord event (fallback to user profile)
		RequirementContext noDiscordContext = new RequirementContext("test", userProfile);
		
		RoleRequirement roleReq = new RoleRequirement(
				RequirementCondition.HAS,
				true,
				Arrays.asList("100", "200"),
				"NAME"
		);
		// When matching by NAME with no Discord context, it falls back to user profile
		// Note: This will actually match by ID since user profile only stores IDs
		// Our user profile has roles [100, 200, 300]
		// Required roles are ["100", "200"] 
		// HAS condition: user must have ALL required roles
		// Result: user has both 100 and 200, so result = true
		assertTrue(evaluator.evaluate(noDiscordContext, roleReq));
	}
}
