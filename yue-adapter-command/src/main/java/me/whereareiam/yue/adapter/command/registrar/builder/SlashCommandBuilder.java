package me.whereareiam.yue.adapter.command.registrar.builder;

import me.whereareiam.yue.api.model.command.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Responsible for creating slash commands (and subcommands) based on a Command config,
 * including parsing the usage string for placeholders and parameters.
 */
@Component
public class SlashCommandBuilder {
	// Patterns for parsing parameters
	private static final Pattern REQUIRED_PARAM_PATTERN = Pattern.compile("\\(([^)]+)\\)");
	private static final Pattern OPTIONAL_PARAM_PATTERN = Pattern.compile("\\[([^\\]]+)\\]");

	/**
	 * Creates a top-level (standalone) slash command from the given Command data.
	 */
	public SlashCommandData buildMainCommand(String commandName, Command command) {
		SlashCommandData slashCommand = Commands.slash(commandName, command.getDescription());

		// Only add options if it is NOT a subcommand
		if (!isSubcommand(command)) {
			addParametersToSlashCommand(slashCommand, command.getUsage());
		}
		return slashCommand;
	}

	/**
	 * Creates a SubcommandData instance for a given alias (e.g., /main help).
	 */
	public SubcommandData buildSubcommand(String subcommandAlias, Command command) {
		SubcommandData subCmd = new SubcommandData(subcommandAlias, command.getDescription());
		addParametersToSubcommand(subCmd, command.getUsage());
		return subCmd;
	}

	/**
	 * Determine if this command is intended to be a subcommand by checking
	 * if its usage string has "{command}" placeholder.
	 */
	public boolean isSubcommand(Command command) {
		String usage = command.getUsage();
		return usage != null && usage.contains("{command}");
	}

	/**
	 * Add parameter options to a top-level slash command by parsing usage text.
	 */
	private void addParametersToSlashCommand(SlashCommandData slashCommand, String usage) {
		List<OptionData> options = parseParameters(usage);
		if (!options.isEmpty()) {
			slashCommand.addOptions(options);
		}
	}

	/**
	 * Add parameter options to a subcommand by parsing usage text.
	 */
	private void addParametersToSubcommand(SubcommandData subCommand, String usage) {
		List<OptionData> options = parseParameters(usage);
		if (!options.isEmpty()) {
			subCommand.addOptions(options);
		}
	}

	/**
	 * Parse parameters from the usage string, extracting required (parentheses)
	 * and optional (brackets) parameters.
	 * <p>
	 * Example usage patterns:
	 * - (user) [channel]
	 * - {command} (query)
	 * <p>
	 * {command} or {alias} are stripped out since they're placeholders.
	 */
	private List<OptionData> parseParameters(String usage) {
		if (usage == null || usage.isBlank()) {
			return Collections.emptyList();
		}

		// Remove placeholders
		String cleanUsage = usage
				.replace("{command}", "")
				.replace("{alias}", "")
				.trim();

		List<OptionData> options = new ArrayList<>();

		// Required: (param)
		Matcher requiredMatcher = REQUIRED_PARAM_PATTERN.matcher(cleanUsage);
		while (requiredMatcher.find()) {
			String name = requiredMatcher.group(1).trim();
			options.add(new OptionData(OptionType.STRING, name, "Required: " + name, true));
		}

		// Optional: [param]
		Matcher optionalMatcher = OPTIONAL_PARAM_PATTERN.matcher(cleanUsage);
		while (optionalMatcher.find()) {
			String name = optionalMatcher.group(1).trim();
			options.add(new OptionData(OptionType.STRING, name, "Optional: " + name, false));
		}

		return options;
	}
}
