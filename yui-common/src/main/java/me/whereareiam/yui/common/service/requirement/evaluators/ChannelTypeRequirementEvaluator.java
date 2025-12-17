package me.whereareiam.yui.common.service.requirement.evaluators;

import me.whereareiam.yui.model.requirement.type.ChannelTypeRequirement;
import me.whereareiam.yui.model.requirement.RequirementEntry;
import me.whereareiam.yui.model.requirement.RequirementContext;
import me.whereareiam.yui.type.requirement.RequirementCondition;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Evaluates channel type requirements.
 */
@Component
public class ChannelTypeRequirementEvaluator extends BaseRequirementEvaluator {
    
    @Override
    public boolean supports(RequirementEntry entry) {
        return entry instanceof ChannelTypeRequirement;
    }
    
    @Override
    protected boolean evaluateInternal(RequirementContext context, RequirementEntry entry) {
        ChannelTypeRequirement channelReq = (ChannelTypeRequirement) entry;
        
        if (channelReq.getTypes() == null || channelReq.getTypes().isEmpty())
            return false;
        
        List<String> requiredTypes = channelReq.getTypes();
        RequirementCondition condition = channelReq.getCondition();
        
        // Get current channel type from original context
        String currentType = extractCurrentChannelType(context);
        
        return switch (condition) {
            case HAS -> requiredTypes.contains(currentType);
            case CONTAINS -> requiredTypes.contains(currentType);
            case EQUALS -> requiredTypes.contains(currentType);
            case GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUALS, LESS_THAN_OR_EQUALS -> false; // Not applicable for channel types
        };
    }
    
    /**
     * Extracts the current channel type from the original context.
     *
     * @param context The context object containing UserProfile and original context
     * @return Current channel type identifier
     */
    private String extractCurrentChannelType(RequirementContext context) {
        Object originalContext = context.getOriginalContext();
        
        if (originalContext instanceof SlashCommandInteractionEvent event) {
            return event.getChannelType().name();
        } else if (originalContext instanceof ButtonInteractionEvent event) {
            return event.getChannelType().name();
        } else if (originalContext instanceof StringSelectInteractionEvent event) {
            return event.getChannelType().name();
        }
        
        return "UNKNOWN";
    }
}
