package me.whereareiam.yui.adapter.command.requirements;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.model.requirement.type.*;
import me.whereareiam.yui.translation.TranslationService;
import me.whereareiam.yui.model.requirement.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for generating user-friendly error messages when command requirements are not met.
 * This is specific to the command framework and not part of the global requirement system.
 */
@Service
@RequiredArgsConstructor
public class RequirementMessageFormatter {
	private final TranslationService translationService;

	/**
	 * Generates a localized error message explaining which requirements failed.
	 *
	 * @param requirements The requirements that were evaluated
	 * @param userId       The user ID for translation
	 * @return A localized error message
	 */
	public String generateErrorMessage(Requirements requirements, long userId) {
		if (requirements == null || requirements.getGroups() == null || requirements.getGroups().isEmpty())
			return translationService.translate("commands.error.requirement.unknown", userId);

		List<String> requirementDescriptions = new ArrayList<>();

		for (RequirementEntry entry : requirements.getGroups().values()) {
			String description = generateEntryDescription(entry, userId);
			if (description != null)
				requirementDescriptions.add(description);
		}

		if (requirementDescriptions.isEmpty())
			return translationService.translate("commands.error.requirement.unknown", userId);

		// Join requirements with appropriate separator
		String requirementsList = String.join(", ", requirementDescriptions);

		return translationService.translate("commands.error.requirement.failed", userId, requirementsList);
	}

	/**
	 * Generates a localized description of a specific requirement with detailed information.
	 */
	private String generateEntryDescription(RequirementEntry entry, long userId) {
		return switch (entry) {
			case RoleRequirement roleReq -> generateRoleRequirementDescription(roleReq, userId);
			case ScopeRequirement scopeReq -> generateScopeRequirementDescription(scopeReq, userId);
			case ChannelTypeRequirement channelReq -> generateChannelRequirementDescription(channelReq, userId);
			case UserRequirement userReq -> generateUserRequirementDescription(userReq, userId);
			case GuildRequirement guildReq -> generateGuildRequirementDescription(guildReq, userId);
			case null, default -> null;
		};
	}

	/**
	 * Generates a detailed description for role requirements.
	 */
	private String generateRoleRequirementDescription(RoleRequirement roleReq, long userId) {
		if (roleReq.getRoles() == null || roleReq.getRoles().isEmpty()) {
			return translationService.translate("commands.error.requirement.role.unknown", userId);
		}

		String roleList = String.join(", ", roleReq.getRoles());
		String matchBy = roleReq.getRoleMatchBy() != null ? roleReq.getRoleMatchBy().toLowerCase() : "id";
		
		return translationService.translate("commands.error.requirement.role", userId, roleList, matchBy);
	}

	/**
	 * Generates a detailed description for scope requirements.
	 */
	private String generateScopeRequirementDescription(ScopeRequirement scopeReq, long userId) {
		if (scopeReq.getScopes() == null || scopeReq.getScopes().isEmpty()) {
			return translationService.translate("commands.error.requirement.scope.unknown", userId);
		}

		String scopeList = String.join(", ", scopeReq.getScopes());
		return translationService.translate("commands.error.requirement.scope", userId, scopeList);
	}

	/**
	 * Generates a detailed description for channel type requirements.
	 */
	private String generateChannelRequirementDescription(ChannelTypeRequirement channelReq, long userId) {
		if (channelReq.getTypes() == null || channelReq.getTypes().isEmpty()) {
			return translationService.translate("commands.error.requirement.channel.unknown", userId);
		}

		String typeList = String.join(", ", channelReq.getTypes());
		return translationService.translate("commands.error.requirement.channel", userId, typeList);
	}

	/**
	 * Generates a detailed description for user requirements.
	 */
	private String generateUserRequirementDescription(UserRequirement userReq, long userId) {
		if (userReq.getUserIds() == null || userReq.getUserIds().isEmpty()) {
			return translationService.translate("commands.error.requirement.user.unknown", userId);
		}

		String userIdList = userReq.getUserIds().stream()
				.map(String::valueOf)
				.collect(Collectors.joining(", "));
		
		return translationService.translate("commands.error.requirement.user", userId, userIdList);
	}

	/**
	 * Generates a detailed description for guild requirements.
	 */
	private String generateGuildRequirementDescription(GuildRequirement guildReq, long userId) {
		if (guildReq.getGuildIds() == null || guildReq.getGuildIds().isEmpty()) {
			return translationService.translate("commands.error.requirement.guild.unknown", userId);
		}

		String guildIdList = guildReq.getGuildIds().stream()
				.map(String::valueOf)
				.collect(Collectors.joining(", "));
		
		return translationService.translate("commands.error.requirement.guild", userId, guildIdList);
	}
}
