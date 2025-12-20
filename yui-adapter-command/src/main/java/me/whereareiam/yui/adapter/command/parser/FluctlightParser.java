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
import org.incendo.cloud.parser.ParserDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Parser that converts a JDA User argument to a Fluctlight.
 * <p>
 * This parser follows the same pattern as JDAParser, extracting the User from OptionMapping
 * and converting it to a Fluctlight using FluctlightService.
 */
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
		
		return jdaInteraction.getOptionMapping(commandInput.readString())
				.map(mapping -> {
					try {
						return fluctlightService.getOrCreate(mapping.getAsUser().getIdLong());
					} catch (final IllegalStateException ignored) {
						return null;
					} catch (final Exception e) {
						// Return null for now - Cloud will handle the error
						return null;
					}
				})
				.map(ArgumentParseResult::successFuture)
				.orElseGet(() -> CompletableFuture.completedFuture(null));
	}

	/**
	 * Creates a parser descriptor for Fluctlight.
	 *
	 * @param fluctlightService The FluctlightService instance
	 * @return Parser descriptor
	 */
	public static ParserDescriptor<Interaction, Fluctlight> fluctlightParser(
			FluctlightService fluctlightService
	) {
		return ParserDescriptor.of(
				new FluctlightParser(fluctlightService),
				Fluctlight.class
		);
	}
}
