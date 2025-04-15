package me.whereareiam.yue.adapter.command.registry;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class CommandRegistry {
	private final Map<String, CommandDefinition> definitions = new HashMap<>();

	public void register(CommandDefinition def) {
		definitions.put(def.getCommandName().toLowerCase(), def);
	}

	public CommandDefinition get(String commandName) {
		return definitions.get(commandName.toLowerCase());
	}

	public Collection<CommandDefinition> getAllCommands() {
		return definitions.values();
	}
}