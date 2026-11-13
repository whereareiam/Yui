package me.whereareiam.yui.adapter.command.permission;

import lombok.RequiredArgsConstructor;
import me.whereareiam.conditia.model.Situation;
import me.whereareiam.conditia.result.Verdict;
import me.whereareiam.yui.adapter.command.definition.CommandDefinitionRegistry;
import me.whereareiam.yui.adapter.command.manager.YuiCommandMetaKeys;
import me.whereareiam.yui.api.command.Interaction;
import me.whereareiam.yui.api.exception.command.RequirementFailedException;
import me.whereareiam.yui.api.model.command.CommandDefinition;
import me.whereareiam.yui.api.permission.Actor;
import me.whereareiam.yui.api.permission.PermissionParts;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import org.incendo.cloud.execution.postprocessor.CommandPostprocessingContext;
import org.incendo.cloud.execution.postprocessor.CommandPostprocessor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Checks a command's {@code requires:} before it runs, with the member who uses it and the interaction, and stops it
 * with {@link RequirementFailedException} when the requirements do not hold.
 */
@Component
@RequiredArgsConstructor
public class CommandPermissionPostprocessor implements CommandPostprocessor<Interaction> {
	private final CommandDefinitionRegistry definitions;
	private final CommandPlaces places;

	@Override
	public void accept(@NotNull CommandPostprocessingContext<Interaction> context) {
		Interaction interaction = context.commandContext().sender();
		GenericCommandInteractionEvent event = interaction.interactionEvent();
		if (event == null) return;

		Optional<String> id = context.command().commandMeta().optional(YuiCommandMetaKeys.DEFINITION);
		Optional<CommandDefinition> definition = id.flatMap(definitions::get);
		if (definition.isEmpty()) return;

		Situation situation = Situation.of(PermissionParts.ACTOR, actor(interaction, event)).and(PermissionParts.INTERACTION, event);
		Verdict verdict = places.place(id.get(), definition.get()).check(situation).toCompletableFuture().join();
		if (!verdict.passed()) throw new RequirementFailedException(id.get(), verdict);
	}

	private static Actor actor(Interaction interaction, GenericCommandInteractionEvent event) {
		Member member = event.getMember();
		if (member == null && interaction.guild() != null) member = interaction.guild().getMember(event.getUser());

		return new Actor(event.getUser(), member, interaction.fluctlight());
	}
}
