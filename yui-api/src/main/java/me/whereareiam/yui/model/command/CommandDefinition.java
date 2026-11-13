package me.whereareiam.yui.model.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.whereareiam.conditia.model.Requirements;
import me.whereareiam.yui.type.CommandCategory;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommandDefinition {
	private boolean enabled;
	private List<String> aliases;
	private String description;
	private String example;
	private String usage;

	private Map<String, String> variables;

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
