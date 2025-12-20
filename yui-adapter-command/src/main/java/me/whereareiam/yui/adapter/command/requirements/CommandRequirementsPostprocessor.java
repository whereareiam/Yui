package me.whereareiam.yui.adapter.command.requirements;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.adapter.command.manager.YuiCommandMetaKeys;
import me.whereareiam.yui.adapter.command.definition.CommandDefinitionRegistry;
import me.whereareiam.yui.exception.command.RequirementFailedException;
import me.whereareiam.yui.command.Interaction;
import me.whereareiam.yui.model.command.CommandDefinition;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.model.requirement.RequirementContext;
import me.whereareiam.yui.model.requirement.Requirements;
import me.whereareiam.yui.requirement.RequirementEvaluator;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import org.incendo.cloud.Command;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.postprocessor.CommandPostprocessingContext;
import org.incendo.cloud.execution.postprocessor.CommandPostprocessor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * Cloud command postprocessor that evaluates Yui command {@link Requirements}
 * before a command is executed.
 * <p>
 * It reuses the global {@link RequirementEvaluator} infrastructure and the
 * {@link CommandDefinitionRegistry} to look up the {@link Requirements} for
 * the invoked command, using the definition ID stored in the command's metadata.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommandRequirementsPostprocessor implements CommandPostprocessor<Interaction> {
	private final RequirementEvaluator requirementEvaluator;
	private final CommandRequirementEvaluatorConfig evaluatorConfig;
	private final CommandDefinitionRegistry definitionRegistry;

	@Override
	public void accept(@NotNull CommandPostprocessingContext<Interaction> context) {
		CommandContext<Interaction> commandContext = context.commandContext();
		Command<Interaction> command = context.command();
		Interaction interaction = commandContext.sender();

		GenericCommandInteractionEvent event = interaction.interactionEvent();
		if (event == null) return;

		// Get the definition ID from the command's metadata (available after parsing)
		String definitionId = command.commandMeta()
				.optional(YuiCommandMetaKeys.DEFINITION)
				.orElse(null);

		if (definitionId == null) return;

		CommandDefinition definition = definitionRegistry.get(definitionId).orElse(null);
		if (definition == null) return;

		Requirements requirements = definition.getRequirements();
		if (requirements == null) return;

		Fluctlight fluctlight = interaction.fluctlight();

		RequirementContext requirementContext = new RequirementContext(event, fluctlight);
		boolean allowed = requirementEvaluator.evaluate(requirementContext, requirements, evaluatorConfig);

		if (allowed) return;

		// Requirements failed: throw RequirementFailedException
		// The exception handler system will catch it and send the appropriate response
		throw new RequirementFailedException(requirements);
	}
}


