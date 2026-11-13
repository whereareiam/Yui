package me.whereareiam.yui.api.exception.command;

import lombok.Getter;
import me.whereareiam.yui.api.command.Interaction;
import me.whereareiam.yui.api.command.exception.ExceptionResponse;
import me.whereareiam.yui.api.exception.command.base.CommandException;
import me.whereareiam.yui.api.message.Translatable;
import me.whereareiam.yui.api.util.style.StyleKit;
import org.jetbrains.annotations.NotNull;

/**
 * A member used a command again before its cooldown ended.
 */
@Getter
public class CooldownActiveException extends CommandException {
	private final long secondsLeft;

	public CooldownActiveException(long secondsLeft) {
		super("The command's cooldown has " + secondsLeft + " seconds left");
		this.secondsLeft = secondsLeft;
	}

	@Override
	public @NotNull ExceptionResponse createResponse(@NotNull Interaction interaction) {
		long user = interaction.fluctlight().getId();

		return ExceptionResponse.embed(StyleKit.embeds().warning()
				.setTitle(Translatable.text("commands.error.cooldown.title").resolve(user))
				.setDescription(Translatable.text("commands.error.cooldown.description").with("seconds", secondsLeft).resolve(user)));
	}
}
