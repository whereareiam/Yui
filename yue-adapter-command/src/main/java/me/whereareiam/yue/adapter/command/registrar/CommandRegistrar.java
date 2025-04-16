package me.whereareiam.yue.adapter.command.registrar;

import me.whereareiam.yue.adapter.command.registrar.builder.SlashCommandBuilder;
import me.whereareiam.yue.api.model.command.Command;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Handles the registration of commands with Discord's JDA API.
 * <p>
 * This service is responsible for translating command configurations into
 * JDA slash command objects and updating them with Discord. It manages both
 * top-level commands and subcommands within the main command group.
 */
@Service
public class CommandRegistrar {
	private static final Logger logger = LoggerFactory.getLogger(CommandRegistrar.class);

	/**
	 * Name of the parent command that hosts subcommands (typically "yue")
	 */
	private static final String MAIN_COMMAND_NAME = "main";

	private final JDA jda;
	private final SlashCommandBuilder slashCommandBuilder;

	/**
	 * Cached command configurations used for bulk registration
	 */
	private final ConcurrentMap<String, Command> cachedCommands = new ConcurrentHashMap<>();

	public CommandRegistrar(JDA jda, SlashCommandBuilder slashCommandBuilder) {
		this.jda = jda;
		this.slashCommandBuilder = slashCommandBuilder;
	}

	/**
	 * Registers a single command with Discord.
	 * <p>
	 * If the command is a subcommand, it will check for the presence of the main command.
	 * All current commands are re-registered via bulk update to ensure consistency.
	 *
	 * @param commandName The internal name of the command
	 * @param command     The command configuration
	 */
	public void registerDiscordCommand(String commandName, Command command) {
		if (command == null || !command.isEnabled()) {
			logger.warn("Attempted to register disabled or null command '{}'. Ignoring.", commandName);
			return;
		}

		cachedCommands.put(commandName, command);

		if (slashCommandBuilder.isSubcommand(command)) {
			Command mainCfg = cachedCommands.get(MAIN_COMMAND_NAME);
			if (mainCfg == null || !mainCfg.isEnabled()) {
				logger.warn("Cannot register sub‑command '{}' because its parent '/yue' is missing or disabled.", commandName);
				return;
			}
		}

		registerDiscordCommands(new HashMap<>(cachedCommands));
	}

	/**
	 * Registers multiple commands with Discord in a single update operation.
	 * <p>
	 * This method handles both top-level commands and subcommands, organizing them
	 * appropriately before sending the update to Discord's API.
	 *
	 * @param commands Map of command names to their configurations
	 */
	public void registerDiscordCommands(Map<String, Command> commands) {
		if (commands == null || commands.isEmpty()) {
			logger.debug("Command map is null or empty. Nothing to register.");
			return;
		}

		cachedCommands.putAll(commands);

		Map<String, SlashCommandData> mainCommands = new HashMap<>();
		Map<String, List<SubcommandData>> subCommands = new HashMap<>();

		Command main = cachedCommands.get(MAIN_COMMAND_NAME);
		if (main != null && main.isEnabled()) {
			for (String alias : main.getAliases()) {
				mainCommands.put(alias, slashCommandBuilder.buildMainCommand(alias, main));
				subCommands.put(alias, new ArrayList<>());
			}
		}

		for (Map.Entry<String, Command> e : cachedCommands.entrySet()) {
			String name = e.getKey();
			Command cfg = e.getValue();
			if (!cfg.isEnabled() || MAIN_COMMAND_NAME.equalsIgnoreCase(name)) continue;

			if (slashCommandBuilder.isSubcommand(cfg) && !mainCommands.isEmpty()) {
				attachSubcommands(cfg, mainCommands, subCommands);
				continue;
			}

			String slashName = cfg.getAliases().isEmpty() ? name : cfg.getAliases().getFirst();
			mainCommands.put(slashName, slashCommandBuilder.buildMainCommand(slashName, cfg));
			subCommands.putIfAbsent(slashName, new ArrayList<>());
		}

		subCommands.forEach((mainAlias, subs) -> {
			if (!subs.isEmpty()) mainCommands.get(mainAlias).addSubcommands(subs);
		});

		List<SlashCommandData> payload = new ArrayList<>(mainCommands.values());
		jda.updateCommands().addCommands(payload).queue(
				_ -> logger.info("Up‑registered {} command(s).", payload.size()),
				error -> logger.error("Failed to register commands", error)
		);
	}

	/**
	 * Attaches subcommands to their parent main commands.
	 *
	 * @param command      The subcommand configuration
	 * @param mainCommands Map of main command names to their SlashCommandData
	 * @param subCommands  Map tracking which subcommands belong to which main commands
	 */
	private void attachSubcommands(Command command, Map<String, SlashCommandData> mainCommands, Map<String, List<SubcommandData>> subCommands) {
		for (String subAlias : command.getAliases()) {
			SubcommandData sub = slashCommandBuilder.buildSubcommand(subAlias, command);
			mainCommands.keySet().forEach(mainAlias ->
					subCommands.computeIfAbsent(mainAlias, k -> new ArrayList<>()).add(sub));
		}
	}
}
