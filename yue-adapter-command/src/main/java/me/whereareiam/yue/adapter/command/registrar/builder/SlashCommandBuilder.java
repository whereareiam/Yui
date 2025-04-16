package me.whereareiam.yue.adapter.command.registrar.builder;

import me.whereareiam.yue.api.component.Translatable;
import me.whereareiam.yue.api.model.command.Command;
import net.dv8tion.jda.api.interactions.DiscordLocale;
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
	private static final Pattern TRANSLATE_PATTERN =
			Pattern.compile("^\\s*translate\\(([^)]+)\\)\\s*$");

	// Patterns for parsing parameters
	private static final Pattern REQUIRED_PARAM_PATTERN = Pattern.compile("\\(([^)]+)\\)");
	private static final Pattern OPTIONAL_PARAM_PATTERN = Pattern.compile("\\[([^\\]]+)\\]");

	/**
	 * Creates a top-level (standalone) slash command from the given Command data.
	 */
	public SlashCommandData buildMainCommand(String commandName, Command command) {
		String description = resolveDescription(command.getDescription());
		SlashCommandData slashCommand = Commands.slash(commandName, description);

		if (!isSubcommand(command))
			addParametersToSlashCommand(slashCommand, command.getUsage());

		return slashCommand;
	}

	/**
	 * Creates a SubcommandData instance for a given alias (e.g., /main help).
	 */
	public SubcommandData buildSubcommand(String subcommandAlias, Command command) {
		String description = resolveDescription(command.getDescription());

		SubcommandData subCommand = new SubcommandData(subcommandAlias, description);
		addParametersToSubcommand(subCommand, command.getUsage());

		return subCommand;
	}

	/**
	 * Determine if this command is intended to be a subcommand by checking
	 * if its usage string has a "{command}" placeholder.
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
			String description = Translatable.of("commands.variables.required", DiscordLocale.ENGLISH_US) + name;
			options.add(new OptionData(OptionType.STRING, name, description, true));
		}

		// Optional: [param]
		Matcher optionalMatcher = OPTIONAL_PARAM_PATTERN.matcher(cleanUsage);
		while (optionalMatcher.find()) {
			String name = optionalMatcher.group(1).trim();
			String description = Translatable.of("commands.variables.optional", DiscordLocale.ENGLISH_US) + name;
			options.add(new OptionData(OptionType.STRING, name, description, false));
		}

		return options;
	}

	/**
	 * Resolves a command description that might contain a translation directive.
	 * <p>
	 * If the description contains a "translate(key)" directive, this method will
	 * extract the key and use the translation service to get the localized text.
	 * Otherwise, it returns the original description unchanged.
	 *
	 * @param raw The raw description text that might contain a translation directive
	 * @return The resolved description text in English locale
	 */
	private static String resolveDescription(String raw) {
		if (raw == null)
			return null;

		Matcher m = TRANSLATE_PATTERN.matcher(raw);
		if (m.matches()) {
			String key = m.group(1).trim();
			return Translatable.of(key, DiscordLocale.ENGLISH_US);
		}
		return raw;
	}
}
