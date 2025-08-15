package me.whereareiam.yui.adapter.command;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.adapter.command.registrar.CommandRegistrar;
import me.whereareiam.yui.adapter.command.registry.CommandDefinition;
import me.whereareiam.yui.adapter.command.registry.CommandRegistry;
import me.whereareiam.yui.adapter.command.scanner.CommandScanner;
import me.whereareiam.yui.api.input.Registry;
import me.whereareiam.yui.api.model.command.Command;
import me.whereareiam.yui.api.model.config.Commands;
import me.whereareiam.yui.api.output.Reloadable;
import me.whereareiam.yui.api.output.config.ConfigurationLoader;
import me.whereareiam.yui.api.output.service.CommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adapts command configuration from a file (via ConfigurationLoader)
 * into an in-memory map and also delegates the registration to JDA
 * (through CommandRegistrar).
 */
@Slf4j
@Service
public class CommandServiceAdapter implements CommandService, Reloadable {
	private final ApplicationContext context;
	private final CommandRegistry commandRegistry;
	private final CommandScanner commandScanner;
	private final CommandRegistrar commandRegistrar;

	private final ConfigurationLoader configLoader;
	private final Path dataPath;

	@Autowired
	public CommandServiceAdapter(
			ApplicationContext context,
			CommandRegistry commandRegistry,
			CommandScanner commandScanner,
			CommandRegistrar commandRegistrar,
			ConfigurationLoader configLoader,
			@Qualifier("dataPath") Path dataPath,
			Registry<Reloadable> reloadableRegistry
	) {
		this.context = context;
		this.commandRegistry = commandRegistry;
		this.commandScanner = commandScanner;
		this.commandRegistrar = commandRegistrar;
		this.configLoader = configLoader;
		this.dataPath = dataPath;

		// Register this service as reloadable
		reloadableRegistry.register(this);
	}

	private void initialize() {
		Commands commands = configLoader.load(dataPath.resolve("commands"), Commands.class);
		register(context, commands);
	}

	@Override
	public void reload() {
		log.debug("Reloading command service");

		// Step 1: Clear the command registry completely
		commandRegistry.clear();

		// Step 2: Re-initialize everything fresh (batch registration performs atomic sync)
		initialize();

		log.debug("Command service reloaded successfully");
	}

	@Override
	public void register(ApplicationContext context, String commandName, Command command) {
		if (command == null || !command.isEnabled())
			return;

		commandRegistry.registerConfig(commandName, command);
		commandScanner.registerBeansInContext(context, commandName);
		commandRegistrar.registerDiscordCommand(commandName, command);
	}

	@Override
	public void register(ApplicationContext context, Map<String, Command> commands) {
		if (commands == null || commands.isEmpty())
			return;

		// Filter to enabled commands only
		Map<String, Command> enabledCommands = commands.entrySet().stream()
				.filter(e -> e.getValue() != null && e.getValue().isEnabled())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		if (enabledCommands.isEmpty()) {
			return;
		}

		// Update local in-memory registry
		commandRegistry.registerConfigs(enabledCommands);
		// Register bean methods in the context
		commandScanner.registerBeansInContext(context, enabledCommands.keySet());
		// Delegate final Discord registration
		commandRegistrar.registerDiscordCommands(enabledCommands);
	}

	@Override
	public void register(ApplicationContext context, Commands commands) {
		if (commands == null || commands.getCommands() == null)
			return;

		register(context, commands.getCommands());
	}

	@Override
	public void unregister(String commandName) {
		CommandDefinition def = commandRegistry.get(commandName);
		if (def == null) {
			log.warn("Cannot unregister. Command '{}' not found in registry.", commandName);
			return;
		}

		// Remove from in-memory registry
		commandRegistry.removeCommand(commandName);

		// Rebuild the entire set of commands to reflect removal
		Map<String, Command> remainingCommands = commandRegistry.getDefinitions().entrySet().stream()
				.filter(e -> e.getValue().getCommandConfig() != null)
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getCommandConfig()));

		// Bulk update in Discord
		commandRegistrar.registerDiscordCommands(remainingCommands);

		log.debug("Unregistered command '{}'.", commandName);
	}

	@Override
	public Command getCommand(String commandName) {
		return commandRegistry.get(commandName).getCommandConfig();
	}

	@Override
	public Map<String, Command> getCommands() {
		return Collections.unmodifiableMap(
				commandRegistry.getDefinitions().entrySet().stream()
						.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getCommandConfig()))
		);
	}
}
