package me.whereareiam.yui.common.requirement.evaluators;

import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.model.fluctlight.FluctlightStateUpdater;
import me.whereareiam.yui.model.requirement.RequirementContext;
import me.whereareiam.yui.model.requirement.type.RoleRequirement;
import me.whereareiam.yui.type.requirement.RequirementCondition;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoleRequirementEvaluatorTest {
	private RoleRequirementEvaluator evaluator;
	private Fluctlight fluctlight;
	private RequirementContext context;

	@Mock
	private SlashCommandInteractionEvent mockEvent;
	@Mock
	private Guild mockGuild;
	@Mock
	private User mockUser;
	@Mock
	private Member mockMember;
	@Mock
	private Role role100;
	@Mock
	private Role role200;
	@Mock
	private Role role300;
	@Mock
	private User jdaUser;
	@Mock
	private JDA mockJda;
	@Mock
	private ObjectProvider<Settings> mockSettingsProvider;

	@BeforeEach
	void setUp() {
		// Mock the settings provider to return null (no settings available)
		when(mockSettingsProvider.getIfAvailable()).thenReturn(null);

		evaluator = new RoleRequirementEvaluator(mockJda, mockSettingsProvider);

		// Setup mock fluctlight
		when(jdaUser.getIdLong()).thenReturn(12345L);
		fluctlight = new Fluctlight(jdaUser);
		FluctlightStateUpdater.updateAllowedRoles(fluctlight, new long[]{100L, 200L, 300L});

		// Setup mock Discord event with roles
		setupMockDiscordEvent();

		// Create context with mock Discord event
		context = new RequirementContext(mockEvent, fluctlight);
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
		RoleRequirement roleReq = new RoleRequirement(RequirementCondition.HAS, true, Arrays.asList("100", "200"), "ID");
		assertTrue(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateHasConditionUserMissingOneRole() {
		RoleRequirement roleReq = new RoleRequirement(RequirementCondition.HAS, true, Arrays.asList("100", "200", "400"), "ID");
		assertFalse(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateContainsCondition() {
		RoleRequirement roleReq = new RoleRequirement(RequirementCondition.CONTAINS, true, Arrays.asList("100", "400", "500"), "ID");
		assertTrue(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateContainsConditionUserHasNoRoles() {
		RoleRequirement roleReq = new RoleRequirement(RequirementCondition.CONTAINS, true, Arrays.asList("400", "500", "600"), "ID");
		assertFalse(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateEqualsCondition() {
		RoleRequirement roleReq = new RoleRequirement(RequirementCondition.EQUALS, true, Arrays.asList("100", "400", "500"), "ID");
		assertTrue(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateWithFluctlightNoRoles() {
		Fluctlight noRolesFluctlight = new Fluctlight(jdaUser);
		FluctlightStateUpdater.updateAllowedRoles(noRolesFluctlight, null);
		RequirementContext noRolesContext = new RequirementContext("test", noRolesFluctlight);

		RoleRequirement roleReq = new RoleRequirement(RequirementCondition.HAS, true, Arrays.asList("100", "200", "300"), "ID");
		assertFalse(evaluator.evaluate(noRolesContext, roleReq));
	}

	@Test
	void testEvaluateWithExpectedFalse() {
		RoleRequirement roleReq = new RoleRequirement(RequirementCondition.HAS, false, Arrays.asList("100", "200"), "ID");
		// Test raw evaluator behavior - this should return true because fluctlight has all required roles
		// The expected=false logic is handled by DefaultRequirementEvaluator, not this evaluator
		assertTrue(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateWithExpectedFalseUserMissingRoles() {
		RoleRequirement roleReq = new RoleRequirement(RequirementCondition.HAS, false, Arrays.asList("100", "200", "400"), "ID");
		// Test raw evaluator behavior - this should return false because fluctlight is missing role 400
		// The expected=false logic is handled by DefaultRequirementEvaluator, not this evaluator
		assertFalse(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateWithNullRoleMatchBy() {
		RoleRequirement roleReq = new RoleRequirement(RequirementCondition.HAS, true, Arrays.asList("100", "200"), null);
		// Should default to ID matching when roleMatchBy is null
		assertTrue(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateWithEmptyRoleMatchBy() {
		RoleRequirement roleReq = new RoleRequirement(RequirementCondition.HAS, true, Arrays.asList("100", "200"), "");
		// Should default to ID matching when roleMatchBy is empty
		assertTrue(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateWithInvalidRoleMatchBy() {
		RoleRequirement roleReq = new RoleRequirement(RequirementCondition.HAS, true, Arrays.asList("100", "200"), "INVALID");
		// Should fallback to ID matching for unknown match types
		assertTrue(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateWithCaseInsensitiveRoleMatchBy() {
		RoleRequirement roleReq = new RoleRequirement(RequirementCondition.HAS, true, Arrays.asList("100", "200"), "id");
		// Should work with lowercase "id"
		assertTrue(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateWithNameRoleMatchBy() {
		RoleRequirement roleReq = new RoleRequirement(RequirementCondition.HAS, true, Arrays.asList("Role100", "Role200"), "NAME");
		// When matching by NAME, it extracts from Discord member context
		// Our mock member has roles [Role100, Role200, Role300]
		// Required roles are ["Role100", "Role200"] 
		// HAS condition: fluctlight must have ALL required roles
		// Result: fluctlight has both Role100 and Role200, so result = true
		assertTrue(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateWithNameRoleMatchByMissingRole() {
		RoleRequirement roleReq = new RoleRequirement(RequirementCondition.HAS, true, Arrays.asList("Role100", "Role400"), "NAME");
		// When matching by NAME, it extracts from Discord member context
		// Our mock member has roles [Role100, Role200, Role300]
		// Required roles are ["Role100", "Role400"] 
		// HAS condition: fluctlight must have ALL required roles
		// Result: fluctlight is missing Role400, so result = false
		assertFalse(evaluator.evaluate(context, roleReq));
	}

	@Test
	void testEvaluateWithNoDiscordContext() {
		// Create context without Discord event (fallback to fluctlight)
		RequirementContext noDiscordContext = new RequirementContext("test", fluctlight);

		RoleRequirement roleReq = new RoleRequirement(RequirementCondition.HAS, true, Arrays.asList("100", "200"), "ID");
		// When there's no Discord context, it falls back to fluctlight
		// Our fluctlight has roles [100, 200, 300]
		// Required roles are ["100", "200"] 
		// HAS condition: fluctlight must have ALL required roles
		// Result: fluctlight has both 100 and 200, so result = true
		assertTrue(evaluator.evaluate(noDiscordContext, roleReq));
	}

	@Test
	void testEvaluateWithNoDiscordContextNameMatching() {
		// Create context without Discord event (fallback to fluctlight)
		RequirementContext noDiscordContext = new RequirementContext("test", fluctlight);

		RoleRequirement roleReq = new RoleRequirement(RequirementCondition.HAS, true, Arrays.asList("100", "200"), "NAME");
		// When matching by NAME with no Discord context, it falls back to fluctlight
		// Note: This will actually match by ID since fluctlight only stores IDs
		// Our fluctlight has roles [100, 200, 300]
		// Required roles are ["100", "200"] 
		// HAS condition: fluctlight must have ALL required roles
		// Result: fluctlight has both 100 and 200, so result = true
		assertTrue(evaluator.evaluate(noDiscordContext, roleReq));
	}
}
