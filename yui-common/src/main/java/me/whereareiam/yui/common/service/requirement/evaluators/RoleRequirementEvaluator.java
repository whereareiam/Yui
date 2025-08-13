package me.whereareiam.yui.common.service.requirement.evaluators;

import me.whereareiam.yui.api.model.requirement.RequirementEntry;
import me.whereareiam.yui.api.model.requirement.RoleRequirement;
import me.whereareiam.yui.api.output.requirement.RequirementContext;
import me.whereareiam.yui.api.type.RequirementCondition;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
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
		String roleMatchBy = roleReq.getRoleMatchBy();

		// Get user's roles based on the match type
		List<String> userRoles = extractUserRoles(context, roleMatchBy);

		return switch (condition) {
			case HAS -> requiredRoles.stream().allMatch(requiredRole ->
					userRoles.stream().anyMatch(userRole -> userRole.equals(requiredRole))); // User must have ALL required roles
			case CONTAINS -> userRoles.stream().anyMatch(userRole ->
					requiredRoles.stream().anyMatch(userRole::equals)); // User must have AT LEAST ONE required role
			case EQUALS ->
					userRoles.stream().anyMatch(requiredRoles::contains); // User must have AT LEAST ONE required role (same as CONTAINS)
			case GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUALS, LESS_THAN_OR_EQUALS ->
					false; // Not applicable for roles
		};
	}

	/**
	 * Extracts user roles from the context based on the specified match type.
	 *
	 * @param context The context object containing UserProfile and original context
	 * @param roleMatchBy The type of matching to use ("ID" or "NAME")
	 * @return List of user role identifiers or names based on the match type
	 */
	private List<String> extractUserRoles(RequirementContext context, String roleMatchBy) {
		// Default to ID matching if not specified
		if (roleMatchBy == null || roleMatchBy.isEmpty())
			roleMatchBy = "ID";

		// If matching by ID, extract role IDs from the member (current Discord state)
		if ("ID".equalsIgnoreCase(roleMatchBy))
			return extractRoleIdsFromContext(context);

		// If matching by NAME, extract role names from the original context
		if ("NAME".equalsIgnoreCase(roleMatchBy))
			return extractRoleNamesFromContext(context);

		// Fallback to ID matching for unknown match types
		return extractRoleIdsFromContext(context);
	}

	/**
	 * Extracts role names from the original context (JDA events).
	 *
	 * @param context The context object containing UserProfile and original context
	 * @return List of user role names
	 */
	private List<String> extractRoleNamesFromContext(RequirementContext context) {
		Object originalContext = context.getOriginalContext();
		
		// Try to extract roles from any event type that has a member
		if (originalContext instanceof SlashCommandInteractionEvent event)
			return extractRolesFromMember(event.getMember());
		
		if (originalContext instanceof ButtonInteractionEvent event)
			return extractRolesFromMember(event.getMember());
		
		if (originalContext instanceof StringSelectInteractionEvent event)
			return extractRolesFromMember(event.getMember());
		
		// Fallback to user profile if no Discord context is available
		return extractRoleNamesFromUserProfile(context);
	}

	/**
	 * Extracts role names from user profile as fallback when Discord context is not available.
	 * Note: This will return role IDs as strings since user profile only stores IDs.
	 */
	private List<String> extractRoleNamesFromUserProfile(RequirementContext context) {
		if (context.getUserProfile().getRoles() == null)
			return List.of();
		
		// User profile only stores role IDs, so we return them as strings
		// This maintains backward compatibility but may not be ideal for name-based matching
		return Arrays.stream(context.getUserProfile().getRoles())
				.mapToObj(String::valueOf)
				.collect(Collectors.toList());
	}

	/**
	 * Extracts role IDs from the original context (JDA events).
	 *
	 * @param context The context object containing UserProfile and original context
	 * @return List of user role IDs
	 */
	private List<String> extractRoleIdsFromContext(RequirementContext context) {
		Object originalContext = context.getOriginalContext();
		
		// Try to extract roles from any event type that has a member
		if (originalContext instanceof SlashCommandInteractionEvent event)
			return extractRoleIdsFromMember(event.getMember());
		
		if (originalContext instanceof ButtonInteractionEvent event)
			return extractRoleIdsFromMember(event.getMember());
		
		if (originalContext instanceof StringSelectInteractionEvent event)
			return extractRoleIdsFromMember(event.getMember());
		
		// Fallback to user profile if no Discord context is available
		return extractRoleIdsFromUserProfile(context);
	}

	/**
	 * Extracts role IDs from user profile as fallback when Discord context is not available.
	 */
	private List<String> extractRoleIdsFromUserProfile(RequirementContext context) {
		if (context.getUserProfile().getRoles() == null)
			return List.of();
		
		return Arrays.stream(context.getUserProfile().getRoles())
				.mapToObj(String::valueOf)
				.collect(Collectors.toList());
	}

	/**
	 * Extracts role names from a member object.
	 */
	private List<String> extractRolesFromMember(Object member) {
		if (member == null)
			return List.of();
		
		// Since we know the member has a getRoles() method that returns a collection,
		// we can cast it safely after checking the type
		if (member instanceof Member jdaMember) {
			return jdaMember.getRoles().stream()
					.map(role -> role.getName())
					.collect(Collectors.toList());
		}
		
		return List.of();
	}

	/**
	 * Extracts role IDs from a member object.
	 */
	private List<String> extractRoleIdsFromMember(Object member) {
		if (member == null)
			return List.of();
		
		// Since we know the member has a getRoles() method that returns a collection,
		// we can cast it safely after checking the type
		if (member instanceof Member jdaMember) {
			return jdaMember.getRoles().stream()
					.map(role -> String.valueOf(role.getIdLong()))
					.collect(Collectors.toList());
		}
		
		return List.of();
	}
}
