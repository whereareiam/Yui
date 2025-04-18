package me.whereareiam.yue.adapter.command.registrar.builder;

import lombok.AllArgsConstructor;
import me.whereareiam.yue.api.component.Translatable;
import me.whereareiam.yue.api.model.command.Command;
import me.whereareiam.yue.api.model.config.settings.Settings;
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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Responsible for creating slash commands (and subcommands) based on a Command config,
 * including parsing the usage string for placeholders and parameters.
 */
@Component
@AllArgsConstructor
public class SlashCommandBuilder {
	private final Settings settings;

	// Pattern for parsing translation directives
	private static final Pattern TRANSLATE_PATTERN =
			Pattern.compile("^\\s*translate\\(([^)]+)\\)\\s*$");

	// Patterns for parsing parameters
	private static final Pattern REQUIRED_PARAM_PATTERN = Pattern.compile("\\(([^)]+)\\)");
	private static final Pattern OPTIONAL_PARAM_PATTERN = Pattern.compile("\\[([^\\]]+)\\]");

	/**
	 * Creates a top-level (standalone) slash command from the given Command data.
	 *
	 * @param commandName The name that will be used for the slash command
	 * @param command     The command configuration containing description and parameters
	 * @return A fully configured SlashCommandData object ready for registration
	 */
	public SlashCommandData buildMainCommand(String commandName, Command command) {
		String description = resolveTranslation(command.getDescription());
		SlashCommandData slash = Commands.slash(commandName, description);

		if (!isSubcommand(command))
			addParametersToSlashCommand(slash, command);

		return slash;
	}

	/**
	 * Creates a SubcommandData instance for a given alias (e.g., /main help).
	 *
	 * @param alias   The name that will be used for the subcommand
	 * @param command The command configuration containing description and parameters
	 * @return A configured SubcommandData object ready to be attached to a parent command
	 */
	public SubcommandData buildSubcommand(String alias, Command command) {
		String description = resolveTranslation(command.getDescription());

		SubcommandData sub = new SubcommandData(alias, description);
		addParametersToSubcommand(sub, command);

		return sub;
	}

	/**
	 * Determines whether a command should be registered as a subcommand.
	 * <p>
	 * A command is considered a subcommand if its usage pattern contains the "{command}" placeholder.
	 *
	 * @param command The command configuration to check
	 * @return true if the command should be registered as a subcommand, false otherwise
	 */
	public boolean isSubcommand(Command command) {
		String usage = command.getUsage();
		return usage != null && usage.contains("{command}");
	}

	/**
	 * Adds parameter options to a top-level slash command by parsing usage text.
	 * <p>
	 * This method extracts required and optional parameters from the usage pattern
	 * and adds them as options to the slash command.
	 *
	 * @param slash   The slash command to add parameters to
	 * @param command The command configuration containing usage pattern and parameter descriptions
	 */
	private void addParametersToSlashCommand(SlashCommandData slash, Command command) {
		List<OptionData> options = parseParameters(command.getUsage(), command.getVariables());
		if (!options.isEmpty()) slash.addOptions(options);
	}

	/**
	 * Adds parameter options to a subcommand by parsing usage text.
	 * <p>
	 * This method extracts required and optional parameters from the usage pattern
	 * and adds them as options to the subcommand.
	 *
	 * @param sub     The subcommand to add parameters to
	 * @param command The command configuration containing usage pattern and parameter descriptions
	 */
	private void addParametersToSubcommand(SubcommandData sub, Command command) {
		List<OptionData> options = parseParameters(command.getUsage(), command.getVariables());
		if (!options.isEmpty()) sub.addOptions(options);
	}

	/**
	 * Parses a usage string to extract parameters and their properties.
	 * <p>
	 * Parameters in parentheses () are required, while parameters in brackets [] are optional.
	 * Parameter descriptions are resolved from the variables map or use the parameter name if not found.
	 *
	 * @param usage     The usage pattern string to parse
	 * @param variables Map of parameter names to their descriptions
	 * @return A list of OptionData objects representing the parsed parameters
	 */
	private List<OptionData> parseParameters(String usage, Map<String, String> variables) {
		if (usage == null || usage.isBlank()) return Collections.emptyList();

		String cleanUsage = usage
				.replace("{command}", "")
				.replace("{alias}", "")
				.trim();

		List<OptionData> opts = new ArrayList<>();

		Matcher required = REQUIRED_PARAM_PATTERN.matcher(cleanUsage);
		while (required.find()) {
			String name = required.group(1).trim();
			String desc = resolveVariableDescription(name, variables);
			opts.add(new OptionData(OptionType.STRING, name, desc, true));
		}

		Matcher optional = OPTIONAL_PARAM_PATTERN.matcher(cleanUsage);
		while (optional.find()) {
			String name = optional.group(1).trim();
			String desc = resolveVariableDescription(name, variables);
			opts.add(new OptionData(OptionType.STRING, name, desc, false));
		}
		return opts;
	}

	/**
	 * Resolves the description for a parameter from the variables map.
	 * <p>
	 * If a description is found in the variables map, it is processed for translations.
	 * If no description is found, the parameter name itself is returned.
	 *
	 * @param param     The parameter name to look up
	 * @param variables Map of parameter names to their descriptions
	 * @return The resolved description, or the parameter name if no description is found
	 */
	private String resolveVariableDescription(String param, Map<String, String> variables) {
		if (variables == null) return param;

		String raw = variables.getOrDefault(param, "").trim();
		if (raw.isEmpty()) return param;

		return resolveTranslation(raw);
	}

	/**
	 * Resolves translation placeholders in text using the translation service.
	 * <p>
	 * This method parses strings that may contain translation directives in the format
	 * "translate(key)" and replaces them with the appropriate translated text.
	 * It uses the default locale (ENGLISH_US) when no specific locale is provided.
	 *
	 * @param raw The raw string that may contain translation directives
	 * @return The translated string, or the original string if no translation was needed
	 * @see me.whereareiam.yue.api.input.translation.TranslationService#translate(String, DiscordLocale)
	 */
	private String resolveTranslation(String raw) {
		if (raw == null) return null;

		Matcher m = TRANSLATE_PATTERN.matcher(raw);
		if (m.matches()) {
			String key = m.group(1).trim();
			return Translatable.of(key, settings.getLocale());
		}

		return raw;
	}
}
