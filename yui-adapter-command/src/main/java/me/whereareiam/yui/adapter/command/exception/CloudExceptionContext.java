package me.whereareiam.yui.adapter.command.exception;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.command.exception.ExceptionContext;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.discord.jda6.JDAInteraction;

/**
 * Implementation of {@link ExceptionContext} that extracts information from Cloud's {@link CommandContext}.
 */
@RequiredArgsConstructor
final class CloudExceptionContext implements ExceptionContext {
	private final CommandContext<JDAInteraction> commandContext;
	
	@Override
	public long getUserId() {
		return commandContext.sender().user().getIdLong();
	}
	
	@Override
	public long getChannelId() {
		JDAInteraction interaction = commandContext.sender();
		if (interaction.interactionEvent() != null && interaction.interactionEvent().getChannel() != null)
			return interaction.interactionEvent().getChannel().getIdLong();

		return 0;
	}
}
