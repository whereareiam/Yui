package me.whereareiam.yui.common.service.requirement.evaluators;

import me.whereareiam.yui.api.model.requirement.GuildRequirement;
import me.whereareiam.yui.api.model.requirement.RequirementEntry;
import me.whereareiam.yui.api.output.requirement.RequirementContext;
import me.whereareiam.yui.api.type.RequirementCondition;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Evaluates guild requirements.
 */
@Component
public class GuildRequirementEvaluator extends BaseRequirementEvaluator {
	@Override
	public boolean supports(RequirementEntry entry) {
		return entry instanceof GuildRequirement;
	}

	@Override
	protected boolean evaluateInternal(RequirementContext context, RequirementEntry entry) {
		GuildRequirement guildReq = (GuildRequirement) entry;

		if (guildReq.getGuildIds() == null || guildReq.getGuildIds().isEmpty())
			return false;

		List<Long> requiredGuildIds = guildReq.getGuildIds();
		RequirementCondition condition = guildReq.getCondition();

		// Get current guild ID from original context
		Long currentGuildId = extractCurrentGuildId(context);
		if (currentGuildId == null)
			return false;

		return switch (condition) {
			case HAS, CONTAINS -> requiredGuildIds.contains(currentGuildId);
			case EQUALS -> requiredGuildIds.contains(currentGuildId);
			case GREATER_THAN -> requiredGuildIds.stream().anyMatch(id -> currentGuildId > id);
			case LESS_THAN -> requiredGuildIds.stream().anyMatch(id -> currentGuildId < id);
			case GREATER_THAN_OR_EQUALS -> requiredGuildIds.stream().anyMatch(id -> currentGuildId >= id);
			case LESS_THAN_OR_EQUALS -> requiredGuildIds.stream().anyMatch(id -> currentGuildId <= id);
		};
	}

	/**
	 * Extracts the current guild ID from the original context.
	 *
	 * @param context The context object containing UserProfile and original context
	 * @return Current guild ID, or null if not available
	 */
	private Long extractCurrentGuildId(RequirementContext context) {
		Object originalContext = context.getOriginalContext();
		
		if (originalContext instanceof SlashCommandInteractionEvent event) {
			return event.getGuild() != null ? event.getGuild().getIdLong() : null;
		} else if (originalContext instanceof ButtonInteractionEvent event) {
			return event.getGuild() != null ? event.getGuild().getIdLong() : null;
		} else if (originalContext instanceof StringSelectInteractionEvent event) {
			return event.getGuild() != null ? event.getGuild().getIdLong() : null;
		}
		
		return null;
	}
}
