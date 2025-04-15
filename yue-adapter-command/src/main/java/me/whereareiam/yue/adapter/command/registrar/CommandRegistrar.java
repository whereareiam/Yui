package me.whereareiam.yue.adapter.command.registrar;

import me.whereareiam.yue.adapter.command.registry.CommandDefinition;
import me.whereareiam.yue.adapter.command.registry.CommandRegistry;
import me.whereareiam.yue.api.model.command.Command;
import me.whereareiam.yue.api.output.service.CommandService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CommandRegistrar {
	private static final Logger logger = LoggerFactory.getLogger(CommandRegistrar.class);

	private final JDA jda;
	private final CommandRegistry registry;
	private final CommandService commandService;

	// Patterns for parsing parameters
	private static final Pattern REQUIRED_PARAM_PATTERN = Pattern.compile("\\(([^)]+)\\)");
	private static final Pattern OPTIONAL_PARAM_PATTERN = Pattern.compile("\\[([^\\]]+)\\]");

	public CommandRegistrar(JDA jda, CommandRegistry registry, CommandService commandService) {
		this.jda = jda;
		this.registry = registry;
		this.commandService = commandService;
	}

	public void registerDiscordCommands() {
		Map<String, SlashCommandData> mainCommands = new HashMap<>();
		Map<String, List<SubcommandData>> subcommandGroups = new HashMap<>();

		// First, identify the main command
		Command mainCommand = commandService.getCommand("main");
		if (mainCommand != null && mainCommand.isEnabled()) {
			// Register main command aliases as top-level commands
			for (String alias : mainCommand.getAliases()) {
				SlashCommandData cmd = Commands.slash(alias, mainCommand.getDescription());
				mainCommands.put(alias, cmd);
				subcommandGroups.put(alias, new ArrayList<>());
				logger.info("Created main command: {}", alias);
			}
		}

		// Process all other commands
		for (CommandDefinition def : registry.getAllCommands()) {
			String commandName = def.getCommandName();
			Command config = def.getCommandConfig();

			if (!config.isEnabled() || "main".equals(commandName))
				continue;

			String usage = config.getUsage();
			boolean isSubcommand = usage.contains("{command}");

			if (isSubcommand && !mainCommands.isEmpty()) {
				// It's a subcommand of a main command
				for (String mainAlias : mainCommands.keySet()) {
					for (String alias : config.getAliases()) {
						SubcommandData subCmd = new SubcommandData(alias, config.getDescription());
						addParametersFromUsage(subCmd, usage);
						subcommandGroups.get(mainAlias).add(subCmd);
						logger.info("Added subcommand '{}' to main command '{}'", alias, mainAlias);
					}
				}
			} else {
				// Standalone command
				SlashCommandData cmd = Commands.slash(commandName, config.getDescription());
				addParametersToCommand(cmd, usage);
				mainCommands.put(commandName, cmd);
				logger.info("Created standalone command: {}", commandName);
			}
		}

		// Add subcommands to main commands
		for (Map.Entry<String, List<SubcommandData>> entry : subcommandGroups.entrySet()) {
			String mainAlias = entry.getKey();
			List<SubcommandData> subcommands = entry.getValue();

			SlashCommandData mainCmd = mainCommands.get(mainAlias);
			if (mainCmd != null && !subcommands.isEmpty()) {
				mainCmd.addSubcommands(subcommands);
			}
		}

		// Register all commands with Discord
		List<SlashCommandData> commands = new ArrayList<>(mainCommands.values());
		jda.updateCommands().addCommands(commands).queue(
				success -> logger.info("Successfully registered {} Discord commands", commands.size()),
				error -> logger.error("Failed to register Discord commands", error)
		);
	}

	private void addParametersToCommand(SlashCommandData cmd, String usage) {
		for (OptionData option : parseParameters(usage))
			cmd.addOptions(option);
	}

	private void addParametersFromUsage(SubcommandData cmd, String usage) {
		for (OptionData option : parseParameters(usage))
			cmd.addOptions(option);
	}

	private List<OptionData> parseParameters(String usage) {
		List<OptionData> options = new ArrayList<>();

		// Strip command and alias placeholders
		String paramPart = usage.replace("{command}", "")
				.replace("{alias}", "")
				.trim();

		// Parse required parameters
		Matcher requiredMatcher = REQUIRED_PARAM_PATTERN.matcher(paramPart);
		while (requiredMatcher.find()) {
			String name = requiredMatcher.group(1);
			options.add(new OptionData(OptionType.STRING, name, "Required parameter: " + name, true));
		}

		// Parse optional parameters
		Matcher optionalMatcher = OPTIONAL_PARAM_PATTERN.matcher(paramPart);
		while (optionalMatcher.find()) {
			String name = optionalMatcher.group(1);
			options.add(new OptionData(OptionType.STRING, name, "Optional parameter: " + name, false));
		}

		return options;
	}
}