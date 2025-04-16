package me.whereareiam.yue.adapter.command.registry;

import lombok.Getter;
import me.whereareiam.yue.api.model.command.Command;
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

	public void registerConfig(String commandName, Command command) {
		definitions.put(
				commandName.toLowerCase(),
				new CommandDefinition(commandName, command)
		);
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

	public CommandDefinition get(String commandName) {
		if (commandName == null) return null;

		return definitions.get(commandName.toLowerCase());
	}

	public boolean isRegistered(String commandName) {
		CommandDefinition def = get(commandName);

		return def != null && def.getBeanInstance() != null;
	}
}
