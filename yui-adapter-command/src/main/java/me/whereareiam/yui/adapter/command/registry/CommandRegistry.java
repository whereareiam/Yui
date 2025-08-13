package me.whereareiam.yui.adapter.command.registry;

import lombok.Getter;
import me.whereareiam.yui.api.model.command.Command;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps an in-memory registry of command definitions (command config + method to invoke).
 */
@Getter
@Service
public class CommandRegistry {
	private final Map<String, CommandDefinition> definitions = new HashMap<>();

	public void update(CommandDefinition definition) {
		definitions.put(definition.getCommandName().toLowerCase(), definition);
	}

	public void registerConfig(String commandName, Command commandConfig) {
		CommandDefinition def = new CommandDefinition(commandName, commandConfig);
		definitions.put(commandName.toLowerCase(), def);

		if (commandConfig.getAliases() != null) {
			for (String alias : commandConfig.getAliases()) {
				definitions.put(alias.toLowerCase(), def);
			}
		}
	}

	public void registerConfigs(Map<String, Command> commandMap) {
		for (Map.Entry<String, Command> entry : commandMap.entrySet()) {
			String commandName = entry.getKey();
			Command command = entry.getValue();

			if (command != null && command.isEnabled()) {
				registerConfig(commandName, command);
			}
		}
	}

	public void removeCommand(String commandName) {
		CommandDefinition def = definitions.remove(commandName.toLowerCase());
		if (def == null) return;

		for (String alias : def.getCommandConfig().getAliases())
			definitions.remove(alias.toLowerCase());
	}

	public CommandDefinition get(String commandName) {
		if (commandName == null) return null;

		return definitions.get(commandName.toLowerCase());
	}

	public boolean isRegistered(String commandName) {
		CommandDefinition def = get(commandName);

		return def != null && def.getBeanInstance() != null;
	}

	/**
	 * Clears all command definitions from the registry.
	 * This is used during reload to ensure a completely fresh start.
	 */
	public void clear() {
		definitions.clear();
	}
}
