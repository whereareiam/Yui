package me.whereareiam.yui.exception.command;

import me.whereareiam.yui.command.exception.ExceptionContext;
import me.whereareiam.yui.command.exception.ExceptionResponse;
import me.whereareiam.yui.exception.command.base.CommandException;
import me.whereareiam.yui.model.requirement.Requirements;
import me.whereareiam.yui.util.style.StyleKit;
import me.whereareiam.yui.translation.Translatable;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

public class RequirementFailedException extends CommandException {
	private final Requirements requirements;
	
	/**
	 * Creates a new requirement failed exception.
	 *
	 * @param requirements The requirements that failed
	 */
	public RequirementFailedException(@NotNull Requirements requirements) {
		super("Command requirements not met");
		this.requirements = requirements;
	}
	
	/**
	 * Returns the requirements that failed.
	 *
	 * @return The requirements
	 */
	@NotNull
	public Requirements getRequirements() {
		return requirements;
	}
	
	@Override
	@NotNull
	public ExceptionResponse createResponse(@NotNull ExceptionContext context) {
		long userId = context.getUserId();
		
		String title = Translatable.forUser("error.requirement.failed.title", userId);
		String description = Translatable.forUser("error.requirement.failed.description", userId);
		
		EmbedBuilder embed = StyleKit.embeds().error()
				.setTitle(title)
				.setDescription(description);
		
		return ExceptionResponse.embed(embed);
	}
}
