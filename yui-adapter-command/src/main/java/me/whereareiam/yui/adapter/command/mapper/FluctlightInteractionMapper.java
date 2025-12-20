package me.whereareiam.yui.adapter.command.mapper;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.fluctlight.FluctlightService;
import me.whereareiam.yui.command.Interaction;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.discord.jda6.JDAInteraction;
import org.springframework.stereotype.Component;

/**
 * Mapper that converts Cloud's JDAInteraction to our Interaction.
 * <p>
 * This mapper is used by Cloud's JDA6CommandManager to convert the internal
 * JDAInteraction to our Interaction sender type.
 */
@Component
@RequiredArgsConstructor
public class FluctlightInteractionMapper implements JDAInteraction.InteractionMapper<Interaction> {
	private final FluctlightService fluctlightService;

	@Override
	public @NonNull Interaction map(@NonNull JDAInteraction cloudInteraction) {
		long userId = cloudInteraction.user().getIdLong();
		var fluctlight = fluctlightService.getOrCreate(userId);

		return new Interaction(
				fluctlight,
				cloudInteraction.guild(),
				cloudInteraction.interactionEvent(),
				cloudInteraction.replyCallback(),
				cloudInteraction.optionMappings()
		);
	}
}

