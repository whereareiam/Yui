package me.whereareiam.yue.api.model.config;

import me.whereareiam.yue.api.model.command.Command;

import java.util.HashMap;
import java.util.Map;

public class Commands {
	private Map<String, Command> commands = new HashMap<>();

	public Map<String, Command> getCommands() {
		return commands;
	}

	public void addCommand(String name, Command command) {
		commands.put(name, command);
	}
}
