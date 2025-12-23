package me.whereareiam.yui.adapter.command.parser;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.command.Interaction;
import me.whereareiam.yui.fluctlight.FluctlightService;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.discord.jda6.JDA6CommandManager;
import org.incendo.cloud.discord.jda6.JDAInteraction;
import org.incendo.cloud.discord.slash.NullableParser;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Parser that converts a JDA User argument to a Fluctlight.
 * <p>
 * This parser follows the same pattern as JDAParser, extracting the User from OptionMapping
 * and converting it to a Fluctlight using FluctlightService.
 */
@Component
@RequiredArgsConstructor
public class FluctlightParser extends NullableParser<Interaction, Fluctlight> {
	private final FluctlightService fluctlightService;

	@Override
	public @NotNull CompletableFuture<ArgumentParseResult<Fluctlight>> parseNullable(
			@NotNull CommandContext<Interaction> commandContext,
			@NotNull CommandInput commandInput
	) {
		// Get the JDAInteraction from context (Cloud stores it there)
		JDAInteraction jdaInteraction = commandContext.get(JDA6CommandManager.CONTEXT_JDA_INTERACTION);
		String optionName = commandInput.readString();

		ArgumentParseResult<Fluctlight> result = jdaInteraction.getOptionMapping(optionName)
				.map(mapping -> {
					try {
						Fluctlight fluctlight = fluctlightService.getOrCreate(mapping.getAsUser().getIdLong());
						return ArgumentParseResult.success(fluctlight);
					} catch (final Exception e) {
						return ArgumentParseResult.<Fluctlight>failure(e);
					}
				})
				.orElseGet(() -> ArgumentParseResult.failure(
						new IllegalArgumentException("Option mapping not found for '" + optionName + "'")
				));

		return CompletableFuture.completedFuture(result);
	}
}
