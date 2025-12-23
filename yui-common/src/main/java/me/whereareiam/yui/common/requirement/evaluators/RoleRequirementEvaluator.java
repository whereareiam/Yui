package me.whereareiam.yui.common.requirement.evaluators;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.model.requirement.RequirementEntry;
import me.whereareiam.yui.model.requirement.RequirementContext;
import me.whereareiam.yui.model.requirement.type.RoleRequirement;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.type.requirement.RequirementCondition;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Evaluates role-based requirements.
 */
@Component
@RequiredArgsConstructor
public class RoleRequirementEvaluator extends BaseRequirementEvaluator {
	private final JDA jda;
	private final ObjectProvider<Settings> settings;

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

		// Get fluctlight's roles based on the match type
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
	 * Extracts fluctlight roles from the context based on the specified match type.
	 *
	 * @param context The context object containing UserProfile and original context
	 * @param roleMatchBy The type of matching to use ("ID" or "NAME")
	 * @return List of fluctlight role identifiers or names based on the match type
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
	 * Extracts role names from the original context (JDA events) or by resolving the member from the configured guild.
	 *
	 * @param context The context object containing UserProfile and original context
	 * @return List of fluctlight role names
	 */
	private List<String> extractRoleNamesFromContext(RequirementContext context) {
		Member member = resolveMember(context);
		if (member != null)
			return extractRolesFromMember(member);

		// Fallback to fluctlight profile if no Discord context is available
		return extractRoleNamesFromUserProfile(context);
	}

	/**
	 * Extracts role names from Fluctlight as fallback when Discord context is not available.
	 * Note: This will return role IDs as strings since Fluctlight only stores allowed role IDs.
	 */
	private List<String> extractRoleNamesFromUserProfile(RequirementContext context) {
		if (context.getFluctlight().getAllowedRoles() == null)
			return List.of();
		
		// Fluctlight only stores allowed role IDs, so we return them as strings
		// This maintains backward compatibility but may not be ideal for name-based matching
		return Arrays.stream(context.getFluctlight().getAllowedRoles())
				.mapToObj(String::valueOf)
				.collect(Collectors.toList());
	}

	/**
	 * Extracts role IDs from the original context (JDA events) or by resolving the member from the configured guild.
	 *
	 * @param context The context object containing UserProfile and original context
	 * @return List of fluctlight role IDs
	 */
	private List<String> extractRoleIdsFromContext(RequirementContext context) {
		Member member = resolveMember(context);
		if (member != null)
			return extractRoleIdsFromMember(member);

		// Fallback to fluctlight profile if no Discord context is available
		return extractRoleIdsFromUserProfile(context);
	}

	/**
	 * Extracts role IDs from Fluctlight as fallback when Discord context is not available.
	 */
	private List<String> extractRoleIdsFromUserProfile(RequirementContext context) {
		if (context.getFluctlight().getAllowedRoles() == null)
			return List.of();
		
		return Arrays.stream(context.getFluctlight().getAllowedRoles())
				.mapToObj(String::valueOf)
				.collect(Collectors.toList());
	}

	/**
	 * Resolves a JDA member either from the interaction event (guild context) or from the configured guild (DM context).
	 */
	private Member resolveMember(RequirementContext context) {
		Object originalContext = context.getOriginalContext();

		// Try to extract member from the event directly
		if (originalContext instanceof SlashCommandInteractionEvent event && event.getMember() != null)
			return event.getMember();

		if (originalContext instanceof ButtonInteractionEvent event && event.getMember() != null)
			return event.getMember();

		if (originalContext instanceof StringSelectInteractionEvent event && event.getMember() != null)
			return event.getMember();

		// Fallback: resolve member from the configured guild (supports DM interactions)
		Settings cfg = settings.getIfAvailable();
		if (cfg == null || cfg.getDiscord() == null)
			return null;

		Guild guild = jda.getGuildById(cfg.getDiscord().getGuildId());
		if (guild == null)
			return null;

		return guild.getMemberById(context.getUserId());
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
					.map(Role::getName)
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
