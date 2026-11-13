package me.whereareiam.yui.api.exception.command;

import lombok.Getter;
import me.whereareiam.conditia.result.Verdict;
import me.whereareiam.yui.api.command.Interaction;
import me.whereareiam.yui.api.command.exception.ExceptionResponse;
import me.whereareiam.yui.api.exception.command.base.CommandException;
import me.whereareiam.yui.api.message.Translatable;
import me.whereareiam.yui.api.util.style.StyleKit;
import org.jetbrains.annotations.NotNull;

/**
 * A command's {@code requires:} did not hold for the member who used it. The reply says why: the text of the failed
 * condition's reason ({@code commands.error.requirement.reasons.<key>}), else the command's own {@code denied}
 * text ({@code commands.<command>.denied}), else Yui's.
 */
@Getter
public class RequirementFailedException extends CommandException {
	private final @NotNull String command;
	private final @NotNull Verdict verdict;

	public RequirementFailedException(@NotNull String command, @NotNull Verdict verdict) {
		super("The requirements of " + command + " do not hold: " + verdict);
		this.command = command;
		this.verdict = verdict;
	}

	@Override
	public @NotNull ExceptionResponse createResponse(@NotNull Interaction interaction) {
		long user = interaction.fluctlight().getId();

		return ExceptionResponse.embed(StyleKit.embeds().error()
				.setTitle(Translatable.text("commands.error.requirement.title").resolve(user))
				.setDescription(description(user)));
	}

	private String description(long user) {
		if (verdict instanceof Verdict.Failed failed && failed.key().isPresent()) {
			String key = "commands.error.requirement.reasons." + failed.key().get();
			String reason = Translatable.text(key).with(failed.reason().orElseThrow().values()).resolve(user);
			if (!reason.equals(key)) return reason;
		}

		String key = "commands." + command + ".denied";
		String denied = Translatable.text(key).resolve(user);

		return denied.equals(key) ? Translatable.text("commands.error.requirement.description").resolve(user) : denied;
	}
}
