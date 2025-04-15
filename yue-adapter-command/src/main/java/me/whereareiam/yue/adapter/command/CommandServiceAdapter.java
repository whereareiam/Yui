package me.whereareiam.yue.adapter.command;

import me.whereareiam.yue.api.model.command.Command;
import me.whereareiam.yue.api.model.config.Commands;
import me.whereareiam.yue.api.output.config.ConfigurationLoader;
import me.whereareiam.yue.api.output.service.CommandService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapts command configuration from a file (via ConfigurationLoader)
 * into an in-memory Map
 */
@Service
public class CommandServiceAdapter implements CommandService {
	// Holds all command configs, keyed by command name
	private final Map<String, Command> commandMap = new HashMap<>();

	public CommandServiceAdapter(ConfigurationLoader configurationLoader,
	                             @Qualifier("dataPath") Path dataPath) {
		Commands loaded = configurationLoader.load(dataPath.resolve("commands"), Commands.class);
		if (loaded != null && loaded.getCommands() != null)
			commandMap.putAll(loaded.getCommands());
	}

	@Override
	public void register(String commandName, Command command) {
		commandMap.put(commandName, command);
	}

	@Override
	public void register(Map<String, Command> commands) {
		commandMap.putAll(commands);
	}

	@Override
	public void register(Commands commands) {
		commandMap.putAll(commands.getCommands());
	}

	@Override
	public void unregister(String commandName) {
		// TODO
	}

	@Override
	public Command getCommand(String commandName) {
		return commandMap.get(commandName);
	}

	@Override
	public Map<String, Command> getCommands() {
		return Collections.unmodifiableMap(commandMap);
	}
}
