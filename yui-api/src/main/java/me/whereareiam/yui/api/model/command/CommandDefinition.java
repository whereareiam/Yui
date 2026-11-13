package me.whereareiam.yui.api.model.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.whereareiam.conditia.model.Requirements;
import me.whereareiam.yui.api.type.CommandCategory;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommandDefinition {
	private boolean enabled;
	private List<String> aliases;

	/**
	 * The command's arguments ({@code {alias} <user> [reason]}); their descriptions, like the command's, are texts
	 * ({@link me.whereareiam.yui.api.command.CommandTexts}).
	 */
	private String usage;
	private CommandCategory category;
	private CommandCooldown cooldown;

	/**
	 * Who may use the command, in Conditia's form; an absent list lets everyone.
	 */
	private Requirements requires = Requirements.none();

	/**
	 * Whether {@code /help} leaves the command out.
	 */
	private boolean hide;
}
