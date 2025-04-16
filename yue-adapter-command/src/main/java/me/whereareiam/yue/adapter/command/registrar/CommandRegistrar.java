package me.whereareiam.yue.adapter.command.registrar;

import me.whereareiam.yue.adapter.command.registrar.builder.SlashCommandBuilder;
import me.whereareiam.yue.api.model.command.Command;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Responsible for registering and updating Slash Commands in Discord via JDA.
 */
@Service
public class CommandRegistrar {
	private static final Logger logger = LoggerFactory.getLogger(CommandRegistrar.class);
	private static final String MAIN_COMMAND_NAME = "main";

	private final JDA jda;
	private final SlashCommandBuilder slashCommandBuilder;

	/**
	 * Keep track of all known top-level slash commands that are currently upserted.
	 * The key is the slash command name; the value is the command data.
	 */
	private final Map<String, SlashCommandData> knownCommands = new HashMap<>();

	public CommandRegistrar(JDA jda, SlashCommandBuilder slashCommandBuilder) {
		this.jda = jda;
		this.slashCommandBuilder = slashCommandBuilder;
	}

	/**
	 * Register or update a single command in Discord. This method internally
	 * calls the more general bulk method to avoid code duplication.
	 */
	public void registerDiscordCommand(String commandName, Command command) {
		if (command == null || !command.isEnabled()) {
			logger.warn("Attempted to register disabled or null command '{}'. Ignoring.", commandName);
			return;
		}

		// Wrap into a map and call bulk registration for consistency
		Map<String, Command> singleEntry = Collections.singletonMap(commandName, command);
		registerDiscordCommandsBulk(singleEntry);
	}

	/**
	 * Bulk registration of multiple commands. This is useful on startup or
	 * when reloading large command sets at once.
	 */
	public void registerDiscordCommandsBulk(Map<String, Command> commandsByName) {
		if (commandsByName == null || commandsByName.isEmpty()) {
			logger.debug("Command map is null or empty. Nothing to register.");
			return;
		}
		logger.info("Registering multiple commands in bulk ({} total).", commandsByName.size());

		// Maps to hold top-level slash commands and subcommands to attach
		Map<String, SlashCommandData> mainCommands = new HashMap<>();
		Map<String, List<SubcommandData>> subCommandsMap = new HashMap<>();

		// Identify "main" command, if present
		Command mainCommand = commandsByName.get(MAIN_COMMAND_NAME);
		if (mainCommand != null && mainCommand.isEnabled()) {
			// For each alias of "main", create a slash command
			for (String alias : mainCommand.getAliases()) {
				SlashCommandData mainSlashCmd = slashCommandBuilder.buildMainCommand(alias, mainCommand);
				mainCommands.put(alias, mainSlashCmd);
				subCommandsMap.put(alias, new ArrayList<>());
			}
		}

		// Process other commands
		for (Map.Entry<String, Command> entry : commandsByName.entrySet()) {
			String cmdName = entry.getKey();
			Command cmdConfig = entry.getValue();

			// Skip any null or disabled config
			if (cmdConfig == null || !cmdConfig.isEnabled()) {
				logger.debug("Skipping disabled/null command: {}", cmdName);
				continue;
			}
			// Skip "main" since we've already handled it
			if (MAIN_COMMAND_NAME.equalsIgnoreCase(cmdName)) {
				continue;
			}

			// Subcommand logic
			if (slashCommandBuilder.isSubcommand(cmdConfig) && !mainCommands.isEmpty()) {
				attachSubcommandsToMainAliases(cmdConfig, mainCommands, subCommandsMap);
				continue;
			}

			// Otherwise, treat as a standalone slash command
			String slashName = cmdConfig.getAliases().isEmpty()
					? cmdName
					: cmdConfig.getAliases().getFirst();

			SlashCommandData standaloneCmd = slashCommandBuilder.buildMainCommand(slashName, cmdConfig);
			mainCommands.put(slashName, standaloneCmd);
			// Ensure subCommandsMap also has an entry for the newly created slash command
			subCommandsMap.putIfAbsent(slashName, new ArrayList<>());
		}

		// Attach subcommands to main commands
		for (Map.Entry<String, List<SubcommandData>> entry : subCommandsMap.entrySet()) {
			String mainAlias = entry.getKey();
			List<SubcommandData> subs = entry.getValue();
			if (!subs.isEmpty()) {
				mainCommands.get(mainAlias).addSubcommands(subs);
			}
		}

		// Perform a single bulk upsert
		List<SlashCommandData> slashList = new ArrayList<>(mainCommands.values());
		jda.updateCommands().addCommands(slashList).queue(
				success -> logger.info("Successfully bulk-registered {} commands.", slashList.size()),
				error -> logger.error("Failed to bulk-register commands", error)
		);

		// Update knownCommands with the newly registered slash commands
		knownCommands.clear();
		for (SlashCommandData scd : slashList) {
			knownCommands.put(scd.getName(), scd);
		}
	}

	/**
	 * For a subcommand command config, attach subcommand(s) to each main alias
	 * that has already been built. Typically used only when we know a valid main
	 * command is present.
	 */
	private void attachSubcommandsToMainAliases(Command subCmdConfig,
	                                            Map<String, SlashCommandData> mainCommands,
	                                            Map<String, List<SubcommandData>> subCommandsMap) {
		for (String alias : subCmdConfig.getAliases()) {
			SubcommandData subCmd = slashCommandBuilder.buildSubcommand(alias, subCmdConfig);
			// Attach to each known main alias
			for (String mainAlias : mainCommands.keySet()) {
				subCommandsMap.computeIfAbsent(mainAlias, k -> new ArrayList<>()).add(subCmd);
			}
		}
	}
}
